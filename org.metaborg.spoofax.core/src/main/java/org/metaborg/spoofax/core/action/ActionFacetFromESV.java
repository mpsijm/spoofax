package org.metaborg.spoofax.core.action;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.metaborg.core.action.CompileGoal;
import org.metaborg.core.action.EndNamedGoal;
import org.metaborg.core.action.ITransformGoal;
import org.metaborg.core.action.NamedGoal;
import org.metaborg.core.action.TransformActionFlags;
import org.metaborg.core.menu.IMenu;
import org.metaborg.core.menu.Menu;
import org.metaborg.core.menu.MenuAction;
import org.metaborg.core.menu.Separator;
import org.metaborg.spoofax.core.esv.ESVReader;
import org.metaborg.spoofax.core.transform.ISpoofaxTransformAction;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public class ActionFacetFromESV {
    private static final ILogger logger = LoggerUtils.logger(ActionFacetFromESV.class);


    public static @Nullable ActionFacet create(IStrategoAppl esv) {
        final Iterable<IStrategoAppl> menuTerms = ESVReader.collectTerms(esv, "ToolbarMenu");
        final Collection<IMenu> menus = Lists.newLinkedList();
        final Multimap<ITransformGoal, ISpoofaxTransformAction> actions = HashMultimap.create();
        final ImmutableList<String> nesting = ImmutableList.of();
        for(IStrategoAppl menuTerm : menuTerms) {
            final IMenu submenu = menu(menuTerm, new TransformActionFlags(), nesting, actions);
            menus.add(submenu);
        }
        addCompileGoal(esv, actions);
        if(menus.isEmpty() && actions.isEmpty()) {
            return null;
        }
        return new ActionFacet(actions, menus);
    }

    private static Menu menu(IStrategoTerm menuTerm, TransformActionFlags flags, ImmutableList<String> nesting,
        Multimap<ITransformGoal, ISpoofaxTransformAction> actions) {
        final String name = name(menuTerm.getSubterm(0));
        final ImmutableList<String> newNesting = ImmutableList.<String>builder().addAll(nesting).add(name).build();
        final TransformActionFlags extraFlags = flags(menuTerm.getSubterm(1));
        final TransformActionFlags mergedFlags = TransformActionFlags.merge(flags, extraFlags);
        final Iterable<IStrategoTerm> items = menuTerm.getSubterm(2);
        final Menu menu = new Menu(name);
        for(IStrategoTerm item : items) {
            final String constructor = Tools.constructorName(item);
            if(constructor == null) {
                logger.error("Could not interpret menu item from term {}", item);
                continue;
            }
            switch(constructor) {
                case "Submenu":
                    final Menu submenu = menu(item, mergedFlags, newNesting, actions);
                    menu.add(submenu);
                    break;
                case "Action":
                    final String actionName = name(item.getSubterm(0));
                    final TransformActionFlags actionFlags = flags(item.getSubterm(2));
                    final TransformActionFlags mergedActionFlags = TransformActionFlags.merge(mergedFlags, actionFlags);
                    final ImmutableList<String> newActionNesting = ImmutableList.<String>builder().addAll(newNesting).add(actionName).build();
                    final NamedGoal goal = new NamedGoal(newActionNesting);
                    final ISpoofaxTransformAction action = transformAction(actionName, goal, mergedActionFlags, item.getSubterm(1));
                    actions.put(goal, action);
                    actions.put(new EndNamedGoal(goal.names.get(goal.names.size() - 1)), action);
                    final MenuAction menuAction = new MenuAction(action);
                    menu.add(menuAction);
                    break;
                case "Separator":
                    final Separator separator = new Separator();
                    menu.add(separator);
                    break;
                default:
                    logger.warn("Unhandled menu item term {}", item);
                    break;
            }
        }
        return menu;
    }
    
    private static ISpoofaxTransformAction transformAction(String name, ITransformGoal goal, TransformActionFlags flags, IStrategoTerm callTerm) {
        switch(Tools.constructorName(callTerm)) {
            case "JavaGenerated":
                return new JavaGeneratedTransformAction(goal, flags);
            case "Java":
                return new JavaTransformAction(name, goal, flags, ESVReader.termContents(callTerm));
            default:
                return new StrategoTransformAction(name, goal, flags, ESVReader.termContents(callTerm));
        }
    }

    private static String name(IStrategoTerm nameTerm) {
        // For some reason, names in menus have different shapes of names that need to be handled:
        // * ToolbarMenu: Label(String("\"Name\""))
        // * Submenu: String("\"Name\"")
        // * Action: String("\"Name\"")
        final IStrategoTerm term;
        if(Tools.hasConstructor((IStrategoAppl) nameTerm, "Label")) {
            term = nameTerm.getSubterm(0);
        } else {
            term = nameTerm;
        }
        return ESVReader.termContents(term);
    }

    private static TransformActionFlags flags(Iterable<IStrategoTerm> flagTerms) {
        final TransformActionFlags flags = new TransformActionFlags();
        for(IStrategoTerm flagTerm : flagTerms) {
            final String constructor = Tools.constructorName(flagTerm);
            if(constructor == null) {
                logger.error("Could not interpret flag from term {}", flagTerm);
                continue;
            }
            switch(constructor) {
                case "Source":
                    flags.parsed = true;
                    break;
                case "OpenEditor":
                    flags.openEditor = true;
                    break;
                case "RealTime":
                    flags.realtime = true;
                    break;
                case "Meta":
                    // Ignore
                    break;
                default:
                    logger.warn("Unhandled flag term {}", flagTerm);
                    break;
            }
        }
        return flags;
    }

    private static void addCompileGoal(IStrategoAppl esv, Multimap<ITransformGoal, ISpoofaxTransformAction> actions) {
        final List<IStrategoAppl> onSaveHandlers = ESVReader.collectTerms(esv, "OnSave");
        if(onSaveHandlers.isEmpty()) {
            return;
        }
        for(IStrategoAppl onSaveHandler : onSaveHandlers) {
            final ITransformGoal goal = new CompileGoal();
            final ISpoofaxTransformAction action = transformAction("Compile", goal, new TransformActionFlags(), onSaveHandler.getSubterm(0));
            actions.put(goal, action);
        }
    }
}
