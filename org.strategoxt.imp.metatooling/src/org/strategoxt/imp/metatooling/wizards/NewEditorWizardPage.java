package org.strategoxt.imp.metatooling.wizards;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (esv).
 */

public class NewEditorWizardPage extends WizardPage {
	
	private Text inputLanguageName;
	
	private Text inputProjectName;
	
	private Text inputPackageName;
	
	private Text inputExtensions;
	
	private boolean isInputProjectNameChanged;
	
	private boolean isInputPackageNameChanged;
	
	private boolean isInputExtensionsChanged;
	
	private boolean ignoreEvents;

	/**
	 * Constructor for SampleNewWizardPage.
	 */
	public NewEditorWizardPage() {
		super("wizardPage");
		setTitle("Spoofax/IMP Editor Project");
		setDescription("This wizard creates a new Spoofax/IMP editor project.");
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 2;
		layout.verticalSpacing = 9;
		
		/*
		Label label = new Label(container, SWT.NULL);
		label.setText("&Container:");
		containerText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		containerText.setLayoutData(gd);
		containerText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		Button button = new Button(container, SWT.PUSH);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});
		*/
				
		new Label(container, SWT.NULL).setText("&Language name:");
		inputLanguageName = new Text(container, SWT.BORDER | SWT.SINGLE);
		inputLanguageName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		inputLanguageName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (!ignoreEvents) {
					distributeLanguageName();
					onChange();
				}
			}
		});
				
		new Label(container, SWT.NULL).setText("&Project name:");
		inputProjectName = new Text(container, SWT.BORDER | SWT.SINGLE);
		inputProjectName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		inputProjectName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (!ignoreEvents) {
					distributeProjectName();
					isInputProjectNameChanged = true;
					onChange();
				}
			}
		});
		
		new Label(container, SWT.NULL).setText("&Plugin ID:");
		inputPackageName = new Text(container, SWT.BORDER | SWT.SINGLE);
		inputPackageName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		inputPackageName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (!ignoreEvents) {
					isInputPackageNameChanged = true;
					onChange();
				}
			}
		});
				
		new Label(container, SWT.NULL).setText("&File extensions:");
		inputExtensions = new Text(container, SWT.BORDER | SWT.SINGLE);
		inputExtensions.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		inputExtensions.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (!ignoreEvents) {
					isInputExtensionsChanged = true;
					onChange();
				}
			}
		});

		setControl(container);
		setPageComplete(false);
		inputLanguageName.setFocus();
	}

	private void distributeLanguageName() {
		if (!isInputProjectNameChanged || getInputProjectName().length() == 0
				|| getInputProjectName().equals(getInputLanguageName())) {
			ignoreEvents = true;
			inputProjectName.setText(getInputLanguageName());
			isInputProjectNameChanged = false;
			ignoreEvents = false;
			distributeProjectName();
		}
		if (!isInputExtensionsChanged || getInputExtensions().length() == 0
				|| getInputExtensions().equals(toExtension(getInputLanguageName()))) {
			ignoreEvents = true;
			inputExtensions.setText(toExtension(getInputLanguageName()));
			isInputExtensionsChanged = false;
			ignoreEvents = false;
		}
	}

	private void distributeProjectName() {
		if (!isInputPackageNameChanged || getInputPackageName().length() == 0
				|| getInputPackageName().equals(toPackageName(getInputProjectName()))) {
			ignoreEvents = true;
			inputPackageName.setText(toPackageName(getInputProjectName()));
			isInputPackageNameChanged = false;
			ignoreEvents = false;
		}
	}

	/**
	 * Ensures that both text fields are set.
	 */
	private void onChange() {		
		if (getInputLanguageName().length() == 0) {
			updateStatus("Language name must be specified");
			return;
		}
		if (getInputProjectName().length() == 0) {
			updateStatus("Project name must be specified");
			return;
		}
		if (getInputLanguageName().replace('\\', '/').indexOf('/', 1) > 0
				|| getInputLanguageName().equals("Common")) {
			updateStatus("Language name must be valid");
			return;
		}
		if (getInputProjectName().replace('\\', '/').indexOf('/', 1) > 0) {
			updateStatus("Project name must be valid");
			return;
		}

		if (getInputPackageName().length() == 0) {
			updateStatus("Package name must be specified");
			return;
		}
		if (!getInputPackageName().toLowerCase().equals(toPackageName(getInputPackageName()))
				|| getInputPackageName().indexOf("..") != -1) {
			updateStatus("Package name must be valid");
			return;
		}

		if (getInputExtensions().length() == 0) {
			updateStatus("File extension must be specified");
			return;
		}
		if (getInputExtensions().indexOf(".") != -1 || getInputExtensions().replace('\\', '/').indexOf("/") != -1
				|| getInputExtensions().indexOf(":") > -1){
			updateStatus("File extension must be valid");
			return;
		}

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		if (workspace.getRoot().getProject(getInputProjectName()).exists()) {
			updateStatus("A project with this name already exists");
			return;
		}
	
		updateStatus(null);
	}
	
	private static String toPackageName(String name) {
		char[] input = name.toLowerCase().replace('-', '_').replace(' ', '_').toCharArray();
		StringBuilder output = new StringBuilder();
		int i = 0;
		while (i < input.length) {
			char c = input[i++];
			if (Character.isLetter(c) || c == '_') {
				output.append(c);
				break;
			}
		}
		while (i < input.length) {
			char c = input[i++];
			if (Character.isLetterOrDigit(c) || c == '_' || c == '.')
				output.append(c);
		}
		return output.toString();
	}
	
	private static String toExtension(String name) {
		String input = name.toLowerCase().replace("-", "").replace(".", "").replace(" ", "").replace(":", "");
		String prefix = input.substring(0, Math.min(input.length(), 3));
		if (input.length() == 0) return "";
		
		for (int i = input.length() - 1;; i--) {
			if (!Character.isDigit(input.charAt(i)) && input.charAt(i) != '.') {
				return prefix + input.substring(Math.max(prefix.length(), Math.min(input.length(), i + 1)));
			} else if (i == prefix.length()) {
				return prefix + input.substring(i);
			}
		}
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}
	
	public String getInputLanguageName() {
		return inputLanguageName.getText().trim();
	}
	
	public String getInputProjectName() {
		return inputProjectName.getText().trim();
	}
	
	public String getInputPackageName() {
		return inputPackageName.getText().trim();
	}
	
	public String getInputExtensions() {
		return inputExtensions.getText().trim();
	}
}