package org.jabref.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TimerTask;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingNode;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;

import org.jabref.Globals;
import org.jabref.JabRefExecutorService;
import org.jabref.gui.actions.ActionFactory;
import org.jabref.gui.actions.Actions;
import org.jabref.gui.actions.ActionsFX;
import org.jabref.gui.actions.AutoLinkFilesAction;
import org.jabref.gui.actions.ConnectToSharedDatabaseCommand;
import org.jabref.gui.actions.ErrorConsoleAction;
import org.jabref.gui.actions.IntegrityCheckAction;
import org.jabref.gui.actions.ManageKeywordsAction;
import org.jabref.gui.actions.MassSetFieldAction;
import org.jabref.gui.actions.MnemonicAwareAction;
import org.jabref.gui.actions.NewDatabaseAction;
import org.jabref.gui.actions.NewEntryAction;
import org.jabref.gui.actions.NewSubDatabaseAction;
import org.jabref.gui.actions.OldDatabaseCommandWrapper;
import org.jabref.gui.actions.OpenBrowserAction;
import org.jabref.gui.actions.SimpleCommand;
import org.jabref.gui.actions.SortTabsAction;
import org.jabref.gui.autosaveandbackup.AutosaveUIManager;
import org.jabref.gui.bibtexkeypattern.BibtexKeyPatternDialog;
import org.jabref.gui.copyfiles.CopyFilesAction;
import org.jabref.gui.customentrytypes.EntryCustomizationDialog;
import org.jabref.gui.customjfx.CustomJFXPanel;
import org.jabref.gui.dbproperties.DatabasePropertiesDialog;
import org.jabref.gui.documentviewer.ShowDocumentViewerAction;
import org.jabref.gui.exporter.ExportCommand;
import org.jabref.gui.exporter.ExportCustomizationDialog;
import org.jabref.gui.exporter.SaveAllAction;
import org.jabref.gui.exporter.SaveDatabaseAction;
import org.jabref.gui.externalfiletype.ExternalFileTypeEditor;
import org.jabref.gui.externalfiletype.ExternalFileTypes;
import org.jabref.gui.groups.EntryTableTransferHandler;
import org.jabref.gui.groups.GroupSidePane;
import org.jabref.gui.help.AboutAction;
import org.jabref.gui.help.HelpAction;
import org.jabref.gui.importer.ImportCommand;
import org.jabref.gui.importer.ImportCustomizationDialog;
import org.jabref.gui.importer.ImportInspectionDialog;
import org.jabref.gui.importer.actions.OpenDatabaseAction;
import org.jabref.gui.importer.fetcher.GeneralFetcher;
import org.jabref.gui.journals.ManageJournalsAction;
import org.jabref.gui.keyboard.KeyBinding;
import org.jabref.gui.keyboard.KeyBindingAction;
import org.jabref.gui.menus.ChangeEntryTypeMenu;
import org.jabref.gui.menus.FileHistoryMenu;
import org.jabref.gui.openoffice.OpenOfficePanel;
import org.jabref.gui.preftabs.PreferencesDialog;
import org.jabref.gui.protectedterms.ProtectedTermsDialog;
import org.jabref.gui.push.PushToApplicationButton;
import org.jabref.gui.push.PushToApplications;
import org.jabref.gui.search.GlobalSearchBar;
import org.jabref.gui.specialfields.SpecialFieldValueViewModel;
import org.jabref.gui.undo.CountingUndoManager;
import org.jabref.gui.util.DefaultTaskExecutor;
import org.jabref.logic.autosaveandbackup.AutosaveManager;
import org.jabref.logic.autosaveandbackup.BackupManager;
import org.jabref.logic.help.HelpFile;
import org.jabref.logic.importer.OutputPrinter;
import org.jabref.logic.importer.ParserResult;
import org.jabref.logic.l10n.Localization;
import org.jabref.logic.search.SearchQuery;
import org.jabref.logic.undo.AddUndoableActionEvent;
import org.jabref.logic.undo.UndoChangeEvent;
import org.jabref.logic.undo.UndoRedoEvent;
import org.jabref.logic.util.OS;
import org.jabref.logic.util.io.FileUtil;
import org.jabref.model.database.BibDatabaseContext;
import org.jabref.model.database.BibDatabaseMode;
import org.jabref.model.database.shared.DatabaseLocation;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.entry.BibtexEntryTypes;
import org.jabref.model.entry.EntryType;
import org.jabref.model.entry.FieldName;
import org.jabref.model.entry.specialfields.SpecialField;
import org.jabref.preferences.JabRefPreferences;
import org.jabref.preferences.LastFocusedTabPreferences;
import org.jabref.preferences.SearchPreferences;

import com.google.common.eventbus.Subscribe;
import org.fxmisc.easybind.EasyBind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import osx.macadapter.MacAdapter;

/**
 * The main window of the application.
 */
public class JabRefFrame extends BorderPane implements OutputPrinter {
    private static final Logger LOGGER = LoggerFactory.getLogger(JabRefFrame.class);

    // Frame titles.
    public static final String FRAME_TITLE = "JabRef";
    private static final String ELLIPSES = "...";
    public final AbstractAction nextTab = new ChangeTabAction(true);
    public final AbstractAction prevTab = new ChangeTabAction(false);
    private final SplitPane splitPane = new SplitPane();
    private final JabRefPreferences prefs = Globals.prefs;
    private final Insets marg = new Insets(1, 0, 2, 0);
    private final IntegrityCheckAction checkIntegrity = new IntegrityCheckAction(this);
    private final ToolBar tlb = new ToolBar();
    private final GlobalSearchBar globalSearchBar = new GlobalSearchBar(this);
    private final JLabel statusLine = new JLabel("", SwingConstants.LEFT);
    private final JLabel statusLabel = new JLabel(
            Localization.lang("Status")
                    + ':', SwingConstants.LEFT);
    private final JProgressBar progressBar = new JProgressBar();
    private final FileHistoryMenu fileHistory = new FileHistoryMenu(prefs, this);

    // Here we instantiate menu/toolbar actions. Actions regarding
    // the currently open database are defined as a GeneralAction
    // with a unique command string. This causes the appropriate
    // BasePanel's runCommand() method to be called with that command.
    // Note: GeneralAction's constructor automatically gets translations
    // for the name and message strings.
    private final AbstractAction keyBindingAction = new KeyBindingAction();
    private final AbstractAction newSubDatabaseAction = new NewSubDatabaseAction(this);
    private final AbstractAction jabrefWebPageAction = new OpenBrowserAction("https://jabref.org",
            Localization.menuTitle("Website"), Localization.lang("Opens JabRef's website"),
            IconTheme.getImage("about"), IconTheme.getImage("about"));
    private final AbstractAction jabrefFacebookAction = new OpenBrowserAction("https://www.facebook.com/JabRef/",
            "Facebook", Localization.lang("Opens JabRef's Facebook page"),
            IconTheme.JabRefIcons.FACEBOOK.getSmallIcon(), IconTheme.JabRefIcons.FACEBOOK.getIcon());
    private final AbstractAction jabrefTwitterAction = new OpenBrowserAction("https://twitter.com/jabref_org",
            "Twitter", Localization.lang("Opens JabRef's Twitter page"),
            IconTheme.JabRefIcons.TWITTER.getSmallIcon(), IconTheme.JabRefIcons.TWITTER.getIcon());
    private final AbstractAction jabrefBlogAction = new OpenBrowserAction("https://blog.jabref.org/",
            Localization.menuTitle("Blog"), Localization.lang("Opens JabRef's blog"),
            IconTheme.JabRefIcons.BLOG.getSmallIcon(), IconTheme.JabRefIcons.BLOG.getIcon());
    private final AbstractAction developmentVersionAction = new OpenBrowserAction("https://builds.jabref.org/master/",
            Localization.menuTitle("Development version"),
            Localization.lang("Opens a link where the current development version can be downloaded"));
    private final AbstractAction changeLogAction = new OpenBrowserAction(
            "https://github.com/JabRef/jabref/blob/master/CHANGELOG.md", Localization.menuTitle("View change log"),
            Localization.lang("See what has been changed in the JabRef versions"));
    private final AbstractAction forkMeOnGitHubAction = new OpenBrowserAction("https://github.com/JabRef/jabref",
            Localization.menuTitle("Fork me on GitHub"), Localization.lang("Opens JabRef's GitHub page"), IconTheme.JabRefIcons.GITHUB.getSmallIcon(), IconTheme.JabRefIcons.GITHUB.getIcon());
    private final AbstractAction donationAction = new OpenBrowserAction("https://donations.jabref.org",
            Localization.menuTitle("Donate to JabRef"), Localization.lang("Donate to JabRef"), IconTheme.JabRefIcons.DONATE.getSmallIcon(), IconTheme.JabRefIcons.DONATE.getIcon());
    private final AbstractAction openForumAction = new OpenBrowserAction("http://discourse.jabref.org/",
            Localization.menuTitle("Online help forum"), Localization.lang("Online help forum"), IconTheme.JabRefIcons.FORUM.getSmallIcon(), IconTheme.JabRefIcons.FORUM.getIcon());
    private final AbstractAction help = new HelpAction(Localization.menuTitle("Online help"), Localization.lang("Online help"),
            HelpFile.CONTENTS, Globals.getKeyPrefs().getKey(KeyBinding.HELP));
    private final AbstractAction about = new AboutAction(Localization.menuTitle("About JabRef"), Localization.lang("About JabRef"),
            IconTheme.getImage("about"));
    private final AbstractAction editEntry = new GeneralAction(Actions.EDIT, Localization.menuTitle("Edit entry"),
            Localization.lang("Edit entry"), Globals.getKeyPrefs().getKey(KeyBinding.EDIT_ENTRY), IconTheme.JabRefIcons.EDIT_ENTRY.getIcon());
    private final AbstractAction focusTable = new GeneralAction(Actions.FOCUS_TABLE,
            Localization.menuTitle("Focus entry table"),
            Localization.lang("Move the keyboard focus to the entry table"), Globals.getKeyPrefs().getKey(KeyBinding.FOCUS_ENTRY_TABLE));
    private final AbstractAction sortTabs = new SortTabsAction(this);
    private final AbstractAction forward = new GeneralAction(Actions.FORWARD, Localization.menuTitle("Forward"),
            Localization.lang("Forward"), Globals.getKeyPrefs().getKey(KeyBinding.FORWARD), IconTheme.JabRefIcons.RIGHT.getIcon());
    private final AbstractAction back = new GeneralAction(Actions.BACK, Localization.menuTitle("Back"),
            Localization.lang("Back"), Globals.getKeyPrefs().getKey(KeyBinding.BACK), IconTheme.JabRefIcons.LEFT.getIcon());
    private final AbstractAction deleteEntry = new GeneralAction(Actions.DELETE, Localization.menuTitle("Delete entry"),
            Localization.lang("Delete entry"), Globals.getKeyPrefs().getKey(KeyBinding.DELETE_ENTRY), IconTheme.JabRefIcons.DELETE_ENTRY.getIcon());
    private final AbstractAction openConsole = new GeneralAction(Actions.OPEN_CONSOLE,
            Localization.menuTitle("Open terminal here"),
            Localization.lang("Open terminal here"),
            Globals.getKeyPrefs().getKey(KeyBinding.OPEN_CONSOLE),
            IconTheme.JabRefIcons.CONSOLE.getIcon());
    private final AbstractAction mark = new GeneralAction(Actions.MARK_ENTRIES, Localization.menuTitle("Mark entries"),
            Localization.lang("Mark entries"), Globals.getKeyPrefs().getKey(KeyBinding.MARK_ENTRIES), IconTheme.JabRefIcons.MARK_ENTRIES.getIcon());
    private final JMenu markSpecific = JabRefFrame.subMenu(Localization.menuTitle("Mark specific color"));
    private final AbstractAction unmark = new GeneralAction(Actions.UNMARK_ENTRIES,
            Localization.menuTitle("Unmark entries"), Localization.lang("Unmark entries"),
            Globals.getKeyPrefs().getKey(KeyBinding.UNMARK_ENTRIES), IconTheme.JabRefIcons.UNMARK_ENTRIES.getIcon());
    private final AbstractAction unmarkAll = new GeneralAction(Actions.UNMARK_ALL, Localization.menuTitle("Unmark all"));
    private final AbstractAction toggleRelevance = new GeneralAction(
            new SpecialFieldValueViewModel(SpecialField.RELEVANCE.getValues().get(0)).getCommand(),
            new SpecialFieldValueViewModel(SpecialField.RELEVANCE.getValues().get(0)).getMenuString(),
            new SpecialFieldValueViewModel(SpecialField.RELEVANCE.getValues().get(0)).getToolTipText(),
            IconTheme.JabRefIcons.RELEVANCE.getIcon());
    private final AbstractAction toggleQualityAssured = new GeneralAction(
            new SpecialFieldValueViewModel(SpecialField.QUALITY.getValues().get(0)).getCommand(),
            new SpecialFieldValueViewModel(SpecialField.QUALITY.getValues().get(0)).getMenuString(),
            new SpecialFieldValueViewModel(SpecialField.QUALITY.getValues().get(0)).getToolTipText(),
            IconTheme.JabRefIcons.QUALITY_ASSURED.getIcon());
    private final AbstractAction togglePrinted = new GeneralAction(
            new SpecialFieldValueViewModel(SpecialField.PRINTED.getValues().get(0)).getCommand(),
            new SpecialFieldValueViewModel(SpecialField.PRINTED.getValues().get(0)).getMenuString(),
            new SpecialFieldValueViewModel(SpecialField.PRINTED.getValues().get(0)).getToolTipText(),
            IconTheme.JabRefIcons.PRINTED.getIcon());
    private final AbstractAction normalSearch = new GeneralAction(Actions.SEARCH, Localization.menuTitle("Search"),
            Localization.lang("Search"), Globals.getKeyPrefs().getKey(KeyBinding.SEARCH), IconTheme.JabRefIcons.SEARCH.getIcon());
    private final AbstractAction manageSelectors = new GeneralAction(Actions.MANAGE_SELECTORS,
            Localization.menuTitle("Manage content selectors"));

    private final AbstractAction editPreamble = new GeneralAction(Actions.EDIT_PREAMBLE,
            Localization.menuTitle("Edit preamble"),
            Localization.lang("Edit preamble"));
    private final AbstractAction editStrings = new GeneralAction(Actions.EDIT_STRINGS,
            Localization.menuTitle("Edit strings"),
            Localization.lang("Edit strings"),
            Globals.getKeyPrefs().getKey(KeyBinding.EDIT_STRINGS),
            IconTheme.JabRefIcons.EDIT_STRINGS.getIcon());
    private final AbstractAction customizeAction = new CustomizeEntryTypeAction();
    private final Action toggleToolbar = enableToggle(new AbstractAction(Localization.menuTitle("Hide/show toolbar")) {

        {
            putValue(Action.SHORT_DESCRIPTION, Localization.lang("Hide/show toolbar"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            tlb.setVisible(!tlb.isVisible());
        }
    });
    private final AbstractAction showPdvViewer = new ShowDocumentViewerAction();
    private final AbstractAction addToGroup = new GeneralAction(Actions.ADD_TO_GROUP, Localization.lang("Add to group") + ELLIPSES);
    private final AbstractAction removeFromGroup = new GeneralAction(Actions.REMOVE_FROM_GROUP,
            Localization.lang("Remove from group") + ELLIPSES);
    private final AbstractAction moveToGroup = new GeneralAction(Actions.MOVE_TO_GROUP, Localization.lang("Move to group") + ELLIPSES);
    private final Action togglePreview = enableToggle(new GeneralAction(Actions.TOGGLE_PREVIEW,
            Localization.menuTitle("Toggle entry preview"),
            Localization.lang("Toggle entry preview"),
            Globals.getKeyPrefs().getKey(KeyBinding.TOGGLE_ENTRY_PREVIEW),
            IconTheme.JabRefIcons.TOGGLE_ENTRY_PREVIEW.getIcon()));
    private final AbstractAction nextPreviewStyle = new GeneralAction(Actions.NEXT_PREVIEW_STYLE,
            Localization.menuTitle("Next preview layout"),
            Globals.getKeyPrefs().getKey(KeyBinding.NEXT_PREVIEW_LAYOUT));
    private final AbstractAction previousPreviewStyle = new GeneralAction(Actions.PREVIOUS_PREVIEW_STYLE,
            Localization.menuTitle("Previous preview layout"),
            Globals.getKeyPrefs().getKey(KeyBinding.PREVIOUS_PREVIEW_LAYOUT));
    private final AbstractAction makeKeyAction = new GeneralAction(Actions.MAKE_KEY,
            Localization.menuTitle("Autogenerate BibTeX keys"),
            Localization.lang("Autogenerate BibTeX keys"),
            Globals.getKeyPrefs().getKey(KeyBinding.AUTOGENERATE_BIBTEX_KEYS),
            IconTheme.JabRefIcons.MAKE_KEY.getIcon());
    private final AbstractAction writeXmpAction = new GeneralAction(Actions.WRITE_XMP,
            Localization.menuTitle("Write XMP-metadata to PDFs"),
            Localization.lang("Will write XMP-metadata to the PDFs linked from selected entries."),
            Globals.getKeyPrefs().getKey(KeyBinding.WRITE_XMP));
    private final AbstractAction openFolder = new GeneralAction(Actions.OPEN_FOLDER,
            Localization.menuTitle("Open folder"), Localization.lang("Open folder"),
            Globals.getKeyPrefs().getKey(KeyBinding.OPEN_FOLDER));
    private final AbstractAction openFile = new GeneralAction(Actions.OPEN_EXTERNAL_FILE,
            Localization.menuTitle("Open file"),
            Localization.lang("Open file"),
            Globals.getKeyPrefs().getKey(KeyBinding.OPEN_FILE),
            IconTheme.JabRefIcons.FILE.getIcon());
    private final AbstractAction openUrl = new GeneralAction(Actions.OPEN_URL,
            Localization.menuTitle("Open URL or DOI"),
            Localization.lang("Open URL or DOI"),
            Globals.getKeyPrefs().getKey(KeyBinding.OPEN_URL_OR_DOI),
            IconTheme.JabRefIcons.WWW.getIcon());
    private final AbstractAction dupliCheck = new GeneralAction(Actions.DUPLI_CHECK,
            Localization.menuTitle("Find duplicates"), IconTheme.JabRefIcons.FIND_DUPLICATES.getIcon());
    private final AbstractAction plainTextImport = new GeneralAction(Actions.PLAIN_TEXT_IMPORT,
            Localization.menuTitle("New entry from plain text") + ELLIPSES,
            Globals.getKeyPrefs().getKey(KeyBinding.NEW_FROM_PLAIN_TEXT));
    private final AbstractAction customExpAction = new CustomizeExportsAction();
    private final AbstractAction customImpAction = new CustomizeImportsAction();
    private final AbstractAction customFileTypesAction = ExternalFileTypeEditor.getAction(this);
    private final AbstractAction autoSetFile = new GeneralAction(Actions.AUTO_SET_FILE,
            Localization.lang("Synchronize file links") + ELLIPSES,
            Globals.getKeyPrefs().getKey(KeyBinding.SYNCHRONIZE_FILES));
    private final AbstractAction abbreviateMedline = new GeneralAction(Actions.ABBREVIATE_MEDLINE,
            Localization.menuTitle("Abbreviate journal names (MEDLINE)"),
            Localization.lang("Abbreviate journal names of the selected entries (MEDLINE abbreviation)"));
    private final AbstractAction abbreviateIso = new GeneralAction(Actions.ABBREVIATE_ISO,
            Localization.menuTitle("Abbreviate journal names (ISO)"),
            Localization.lang("Abbreviate journal names of the selected entries (ISO abbreviation)"),
            Globals.getKeyPrefs().getKey(KeyBinding.ABBREVIATE));
    private final AbstractAction unabbreviate = new GeneralAction(Actions.UNABBREVIATE,
            Localization.menuTitle("Unabbreviate journal names"),
            Localization.lang("Unabbreviate journal names of the selected entries"),
            Globals.getKeyPrefs().getKey(KeyBinding.UNABBREVIATE));
    private final AbstractAction exportLinkedFiles = new CopyFilesAction();
    private final AbstractAction manageJournals = new ManageJournalsAction();
    private final AbstractAction databaseProperties = new DatabasePropertiesAction();
    private final AbstractAction bibtexKeyPattern = new BibtexKeyPatternAction();
    private final AbstractAction errorConsole = new ErrorConsoleAction();
    private final AbstractAction cleanupEntries = new GeneralAction(Actions.CLEANUP,
            Localization.menuTitle("Cleanup entries") + ELLIPSES,
            Localization.lang("Cleanup entries"),
            Globals.getKeyPrefs().getKey(KeyBinding.CLEANUP),
            IconTheme.JabRefIcons.CLEANUP_ENTRIES.getIcon());
    private final AbstractAction mergeEntries = new GeneralAction(Actions.MERGE_ENTRIES,
            Localization.menuTitle("Merge entries") + ELLIPSES,
            Localization.lang("Merge entries"),
            IconTheme.JabRefIcons.MERGE_ENTRIES.getIcon());
    private final AbstractAction downloadFullText = new GeneralAction(Actions.DOWNLOAD_FULL_TEXT,
            Localization.menuTitle("Look up full text documents"),
            Globals.getKeyPrefs().getKey(KeyBinding.DOWNLOAD_FULL_TEXT));
    private final AbstractAction increaseFontSize = new IncreaseTableFontSizeAction();
    private final AbstractAction defaultFontSize = new DefaultTableFontSizeAction();
    private final AbstractAction decreseFontSize = new DecreaseTableFontSizeAction();
    private final AbstractAction resolveDuplicateKeys = new GeneralAction(Actions.RESOLVE_DUPLICATE_KEYS,
            Localization.menuTitle("Resolve duplicate BibTeX keys"),
            Localization.lang("Find and remove duplicate BibTeX keys"),
            Globals.getKeyPrefs().getKey(KeyBinding.RESOLVE_DUPLICATE_BIBTEX_KEYS));

    private final JMenu lookupIdentifiers = JabRefFrame.subMenu(Localization.menuTitle("Look up document identifier..."));
    private final GeneralAction findUnlinkedFiles = new GeneralAction(
            Actions.findUnlinkedFiles,
            FindUnlinkedFilesDialog.ACTION_MENU_TITLE, FindUnlinkedFilesDialog.ACTION_SHORT_DESCRIPTION,
            Globals.getKeyPrefs().getKey(KeyBinding.FIND_UNLINKED_FILES)
    );
    private final AutoLinkFilesAction autoLinkFile = new AutoLinkFilesAction();
    // The action for adding a new entry of unspecified type.
    private final NewEntryAction newEntryAction = new NewEntryAction(this, Globals.getKeyPrefs().getKey(KeyBinding.NEW_ENTRY));
    private final List<NewEntryAction> newSpecificEntryAction = getNewEntryActions();
    // The action for closing the current database and leaving the window open.
    private final CloseAllDatabasesAction closeAllDatabasesAction = new CloseAllDatabasesAction();
    private final CloseOtherDatabasesAction closeOtherDatabasesAction = new CloseOtherDatabasesAction();
    // The action for opening the preferences dialog.
    private final AbstractAction showPrefs = new ShowPrefsAction();
    // Lists containing different subsets of actions for different purposes
    private final List<Object> specialFieldButtons = new LinkedList<>();
    private final List<Object> openDatabaseOnlyActions = new LinkedList<>();
    private final List<Object> severalDatabasesOnlyActions = new LinkedList<>();
    private final List<Object> openAndSavedDatabasesOnlyActions = new LinkedList<>();
    private final List<Object> sharedDatabaseOnlyActions = new LinkedList<>();
    private final List<Object> noSharedDatabaseActions = new LinkedList<>();
    private final List<Object> oneEntryOnlyActions = new LinkedList<>();
    private final List<Object> oneEntryWithFileOnlyActions = new LinkedList<>();
    private final List<Object> oneEntryWithURLorDOIOnlyActions = new LinkedList<>();
    private final List<Object> twoEntriesOnlyActions = new LinkedList<>();
    private final List<Object> atLeastOneEntryActions = new LinkedList<>();
    private PreferencesDialog prefsDialog;
    // The sidepane manager takes care of populating the sidepane.
    private SidePaneManager sidePaneManager;
    private final TabPane tabbedPane = new TabPane();
    /* References to the toggle buttons in the toolbar */
    private JToggleButton previewToggle;
    private JMenu rankSubMenu;
    private PushToApplicationButton pushExternalButton;
    private PushToApplications pushApplications;
    private GeneralFetcher generalFetcher;
    private OpenOfficePanel openOfficePanel;
    private GroupSidePane groupSidePane;
    private JMenu newSpec;
    private final CountingUndoManager undoManager = new CountingUndoManager();

    public JabRefFrame() {
        init();
    }

    private static Action enableToggle(Action a, boolean initialValue) {
        // toggle only works correctly when the SELECTED_KEY is set to false or true explicitly upon start
        a.putValue(Action.SELECTED_KEY, String.valueOf(initialValue));

        return a;
    }

    private static Action enableToggle(Action a) {
        return enableToggle(a, false);
    }

    public static JMenu subMenu(String name) {
        int i = name.indexOf('&');
        JMenu res;
        if (i >= 0) {
            res = new JMenu(name.substring(0, i) + name.substring(i + 1));
            char mnemonic = Character.toUpperCase(name.charAt(i + 1));
            res.setMnemonic((int) mnemonic);
        } else {
            res = new JMenu(name);
        }

        return res;
    }

    /**
     * Takes a list of Object and calls the method setEnabled on them, depending on whether it is an Action or a
     * Component.
     *
     * @param list List that should contain Actions and Components.
     */
    private static void setEnabled(List<Object> list, boolean enabled) {
        for (Object actionOrComponent : list) {
            if (actionOrComponent instanceof Action) {
                ((Action) actionOrComponent).setEnabled(enabled);
            }
            if (actionOrComponent instanceof Component) {
                ((Component) actionOrComponent).setEnabled(enabled);
                if (actionOrComponent instanceof JPanel) {
                    JPanel root = (JPanel) actionOrComponent;
                    for (int index = 0; index < root.getComponentCount(); index++) {
                        root.getComponent(index).setEnabled(enabled);
                    }
                }
            }
        }
    }

    private List<NewEntryAction> getNewEntryActions() {
        // only Bibtex
        List<NewEntryAction> actions = new ArrayList<>();
        for (EntryType type : BibtexEntryTypes.ALL) {
            KeyStroke keyStroke = new ChangeEntryTypeMenu(Globals.getKeyPrefs()).entryShortCuts.get(type.getName());
            if (keyStroke == null) {
                actions.add(new NewEntryAction(this, type.getName()));
            } else {
                actions.add(new NewEntryAction(this, type.getName(), keyStroke));
            }
        }
        return actions;
    }

    private JPopupMenu tabPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();

        // Close actions
        JMenuItem close = new JMenuItem(Localization.lang("Close"));
        JMenuItem closeOthers = new JMenuItem(Localization.lang("Close others"));
        JMenuItem closeAll = new JMenuItem(Localization.lang("Close all"));
        //close.addActionListener(closeDatabaseAction);
        closeOthers.addActionListener(closeOtherDatabasesAction);
        closeAll.addActionListener(closeAllDatabasesAction);
        popupMenu.add(close);
        popupMenu.add(closeOthers);
        popupMenu.add(closeAll);

        popupMenu.addSeparator();

        JMenuItem databasePropertiesMenu = new JMenuItem(Localization.lang("Library properties"));
        databasePropertiesMenu.addActionListener(this.databaseProperties);
        popupMenu.add(databasePropertiesMenu);

        JMenuItem bibtexKeyPatternBtn = new JMenuItem(Localization.lang("BibTeX key patterns"));
        bibtexKeyPatternBtn.addActionListener(bibtexKeyPattern);
        popupMenu.add(bibtexKeyPatternBtn);

        return popupMenu;
    }

    private void init() {

        // TODO: popup
        // tabbedPane = new DragDropPopupPane(tabPopupMenu());

        initSidePane();

        initLayout();

        initActions();

        // Show the toolbar if it was visible at last shutdown:
        tlb.setVisible(Globals.prefs.getBoolean(JabRefPreferences.TOOLBAR_VISIBLE));

        //setBounds(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());
        //WindowLocation pw = new WindowLocation(this, JabRefPreferences.POS_X, JabRefPreferences.POS_Y, JabRefPreferences.SIZE_X,
        //        JabRefPreferences.SIZE_Y);
        //pw.displayWindowAtStoredLocation();

        tabbedPane.setBorder(null);
        // TODO: Color
        //tabbedPane.setForeground(GUIGlobals.INACTIVE_TABBED_COLOR);

        /*
         * The following state listener makes sure focus is registered with the
         * correct database when the user switches tabs. Without this,
         * cut/paste/copy operations would some times occur in the wrong tab.
         */
        EasyBind.subscribe(tabbedPane.getSelectionModel().selectedItemProperty(), e -> {
            if (e == null) {
                return;
            }

            BasePanel currentBasePanel = getCurrentBasePanel();
            if (currentBasePanel == null) {
                return;
            }

            // Poor-mans binding to global state
            // We need to invoke this in the JavaFX thread as all the listeners sit there
            Platform.runLater(() ->
                    Globals.stateManager.activeDatabaseProperty().setValue(Optional.of(currentBasePanel.getBibDatabaseContext()))
            );
            if (new SearchPreferences(Globals.prefs).isGlobalSearch()) {
                globalSearchBar.performSearch();
            } else {
                String content = "";
                Optional<SearchQuery> currentSearchQuery = currentBasePanel.getCurrentSearchQuery();
                if (currentSearchQuery.isPresent()) {
                    content = currentSearchQuery.get().getQuery();
                }
                globalSearchBar.setSearchTerm(content);
            }

            currentBasePanel.getPreviewPanel().updateLayout(Globals.prefs.getPreviewPreferences());

            // groupSidePane.getToggleAction().setSelected(sidePaneManager.isComponentVisible(GroupSidePane.class));
            //previewToggle.setSelected(Globals.prefs.getPreviewPreferences().isPreviewPanelEnabled());
            //generalFetcher.getToggleAction().setSelected(sidePaneManager.isComponentVisible(GeneralFetcher.class));
            //openOfficePanel.getToggleAction().setSelected(sidePaneManager.isComponentVisible(OpenOfficeSidePanel.class));
            // TODO: Can't notify focus listener since it is expecting a swing component
            //Globals.getFocusListener().setFocused(currentBasePanel.getMainTable());
            setWindowTitle();
            // Update search autocompleter with information for the correct database:
            currentBasePanel.updateSearchManager();
            // Set correct enabled state for Back and Forward actions:
            currentBasePanel.setBackAndForwardEnabledState();
            currentBasePanel.getUndoManager().postUndoRedoEvent();
            currentBasePanel.getMainTable().requestFocus();
        });

        //Note: The registration of Apple event is at the end of initialization, because
        //if the events happen too early (ie when the window is not initialized yet), the
        //opened (double-clicked) documents are not displayed.
        if (OS.OS_X) {
            try {
                new MacAdapter().registerMacEvents(this);
            } catch (Exception e) {
                LOGGER.error("Could not interface with Mac OS X methods.", e);
            }
        }

        initShowTrackingNotification();

    }

    private void initShowTrackingNotification() {
        if (!Globals.prefs.shouldAskToCollectTelemetry()) {
            JabRefExecutorService.INSTANCE.submit(new TimerTask() {

                @Override
                public void run() {
                    SwingUtilities.invokeLater(() -> {
                        DefaultTaskExecutor.runInJavaFXThread(JabRefFrame.this::showTrackingNotification);
                    });
                }
            }, 60000); // run in one minute
        }
    }

    private Void showTrackingNotification() {
        if (!Globals.prefs.shouldCollectTelemetry()) {
            DialogService dialogService = new FXDialogService();
            boolean shouldCollect = dialogService.showConfirmationDialogAndWait(
                    Localization.lang("Telemetry: Help make JabRef better"),
                    Localization.lang("To improve the user experience, we would like to collect anonymous statistics on the features you use. We will only record what features you access and how often you do it. We will neither collect any personal data nor the content of bibliographic items. If you choose to allow data collection, you can later disable it via Options -> Preferences -> General."),
                    Localization.lang("Share anonymous statistics"),
                    Localization.lang("Don't share"));
            Globals.prefs.setShouldCollectTelemetry(shouldCollect);
        }

        Globals.prefs.askedToCollectTelemetry();

        return null;
    }

    public void refreshTitleAndTabs() {
        setWindowTitle();
        updateAllTabTitles();
    }

    /**
     * Sets the title of the main window.
     */
    public void setWindowTitle() {
        BasePanel panel = getCurrentBasePanel();

        // no database open
        if (panel == null) {
            //setTitle(FRAME_TITLE);
            return;
        }

        String mode = panel.getBibDatabaseContext().getMode().getFormattedName();
        String modeInfo = String.format(" (%s)", Localization.lang("%0 mode", mode));
        boolean isAutosaveEnabled = Globals.prefs.getBoolean(JabRefPreferences.LOCAL_AUTO_SAVE);

        if (panel.getBibDatabaseContext().getLocation() == DatabaseLocation.LOCAL) {
            String changeFlag = panel.isModified() && !isAutosaveEnabled ? "*" : "";
            String databaseFile = panel.getBibDatabaseContext().getDatabaseFile().map(File::getPath)
                    .orElse(GUIGlobals.UNTITLED_TITLE);
            //setTitle(FRAME_TITLE + " - " + databaseFile + changeFlag + modeInfo);
        } else if (panel.getBibDatabaseContext().getLocation() == DatabaseLocation.SHARED) {
            //setTitle(FRAME_TITLE + " - " + panel.getBibDatabaseContext().getDBMSSynchronizer().getDBName() + " ["
            //        + Localization.lang("shared") + "]" + modeInfo);
        }
    }

    private void initSidePane() {
        sidePaneManager = new SidePaneManager(this);

        groupSidePane = new GroupSidePane(this, sidePaneManager);
        if (prefs.getBoolean(JabRefPreferences.GROUP_SIDEPANE_VISIBLE)) {
            sidePaneManager.register(groupSidePane);
            sidePaneManager.show(GroupSidePane.class);
        }
        openOfficePanel = new OpenOfficePanel(this, sidePaneManager);
        generalFetcher = new GeneralFetcher(this, sidePaneManager);

        sidePaneManager.register(groupSidePane);
    }

    /**
     * The MacAdapter calls this method when a "BIB" file has been double-clicked from the Finder.
     */
    public void openAction(String filePath) {
        Path file = Paths.get(filePath);
        // all the logic is done in openIt. Even raising an existing panel
        getOpenDatabaseAction().openFile(file, true);
    }

    // General info dialog.  The MacAdapter calls this method when "About"
    // is selected from the application menu.
    public void about() {
        // reuse the normal about action
        // null as parameter is OK as the code of actionPerformed does not rely on the data sent in the event.
        about.actionPerformed(null);
    }

    // General preferences dialog.  The MacAdapter calls this method when "Preferences..."
    // is selected from the application menu.
    public void showPreferencesDialog() {
        output(Localization.lang("Opening preferences..."));
        if (prefsDialog == null) {
            prefsDialog = new PreferencesDialog(JabRefFrame.this);
            //prefsDialog.setLocationRelativeTo(JabRefFrame.this);
        } else {
            prefsDialog.setValues();
        }

        prefsDialog.setVisible(true);
        output("");
    }

    public JabRefPreferences prefs() {
        return prefs;
    }

    /**
     * Tears down all things started by JabRef
     * <p>
     * FIXME: Currently some threads remain and therefore hinder JabRef to be closed properly
     *
     * @param filenames the filenames of all currently opened files - used for storing them if prefs openLastEdited is set to true
     */
    private void tearDownJabRef(List<String> filenames) {
        Globals.stopBackgroundTasks();
        Globals.shutdownThreadPools();

        //dispose();

        //prefs.putBoolean(JabRefPreferences.WINDOW_MAXIMISED, getExtendedState() == Frame.MAXIMIZED_BOTH);

        prefs.putBoolean(JabRefPreferences.TOOLBAR_VISIBLE, tlb.isVisible());
        // Store divider location for side pane:
        double width = splitPane.getDividerPositions()[0];
        if (width > 0) {
            prefs.putDouble(JabRefPreferences.SIDE_PANE_WIDTH, width);
        }
        if (prefs.getBoolean(JabRefPreferences.OPEN_LAST_EDITED)) {
            // Here we store the names of all current files. If
            // there is no current file, we remove any
            // previously stored filename.
            if (filenames.isEmpty()) {
                prefs.remove(JabRefPreferences.LAST_EDITED);
            } else {
                prefs.putStringList(JabRefPreferences.LAST_EDITED, filenames);
                File focusedDatabase = getCurrentBasePanel().getBibDatabaseContext().getDatabaseFile().orElse(null);
                new LastFocusedTabPreferences(prefs).setLastFocusedTab(focusedDatabase);
            }

        }

        fileHistory.storeHistory();
        prefs.customExports.store(Globals.prefs);
        prefs.customImports.store();

        prefs.flush();

        // dispose all windows, even if they are not displayed anymore
        for (Window window : Window.getWindows()) {
            window.dispose();
        }
    }

    /**
     * General info dialog.  The MacAdapter calls this method when "Quit"
     * is selected from the application menu, Cmd-Q is pressed, or "Quit" is selected from the Dock.
     * The function returns a boolean indicating if quitting is ok or not.
     * <p>
     * Non-OSX JabRef calls this when choosing "Quit" from the menu
     * <p>
     * SIDE EFFECT: tears down JabRef
     *
     * @return true if the user chose to quit; false otherwise
     */
    public boolean quit() {
        // Ask here if the user really wants to close, if the base
        // has not been saved since last save.
        boolean close = true;

        List<String> filenames = new ArrayList<>();
        for (int i = 0; i < tabbedPane.getTabs().size(); i++) {
                BibDatabaseContext context = getBasePanelAt(i).getBibDatabaseContext();

                if (getBasePanelAt(i).isModified() && (context.getLocation() == DatabaseLocation.LOCAL)) {
                    tabbedPane.getSelectionModel().select(i);
                    String filename = context.getDatabaseFile().map(File::getAbsolutePath).orElse(GUIGlobals.UNTITLED_TITLE);
                    int answer = showSaveDialog(filename);

                    if ((answer == JOptionPane.CANCEL_OPTION) ||
                            (answer == JOptionPane.CLOSED_OPTION)) {
                        return false;
                    }
                    if (answer == JOptionPane.YES_OPTION) {
                        // The user wants to save.
                        try {
                            //getCurrentBasePanel().runCommand("save");
                            SaveDatabaseAction saveAction = new SaveDatabaseAction(getCurrentBasePanel());
                            saveAction.runCommand();
                            if (saveAction.isCanceled() || !saveAction.isSuccess()) {
                                // The action was either canceled or unsuccessful.
                                // Break!
                                output(Localization.lang("Unable to save library"));
                                close = false;
                            }
                        } catch (Throwable ex) {
                            // Something prevented the file
                            // from being saved. Break!!!
                            close = false;
                            break;
                        }
                    }
                } else if (context.getLocation() == DatabaseLocation.SHARED) {
                    context.convertToLocalDatabase();
                    context.getDBMSSynchronizer().closeSharedDatabase();
                    context.clearDBMSSynchronizer();
                }
                AutosaveManager.shutdown(context);
                BackupManager.shutdown(context);
                context.getDatabaseFile().map(File::getAbsolutePath).ifPresent(filenames::add);
            }

        if (close) {
            for (int i = 0; i < tabbedPane.getTabs().size(); i++) {
                if (getBasePanelAt(i).isSaving()) {
                    // There is a database still being saved, so we need to wait.
                    WaitForSaveOperation w = new WaitForSaveOperation(this);
                    w.show(); // This method won't return until canceled or the save operation is done.
                    if (w.canceled()) {
                        return false; // The user clicked cancel.
                    }
                }
            }

            tearDownJabRef(filenames);
            return true;
        }

        return false;
    }

    private void initLayout() {

        setProgressBarVisible(false);

        pushApplications = new PushToApplications();
        pushExternalButton = new PushToApplicationButton(this, pushApplications.getApplications());
        //createToolBar();
        setTop(createMenu());
        //getContentPane().setLayout(new BorderLayout());

        JPanel toolbarPanel = new JPanel(new WrapLayout(FlowLayout.LEFT));
        toolbarPanel.add(tlb);
        toolbarPanel.add(globalSearchBar);
        //getContentPane().add(toolbarPanel, BorderLayout.PAGE_START);

        JFXPanel tabbedPaneContainer = CustomJFXPanel.wrap(new Scene(tabbedPane));
        // TODO: Remove this hack as soon as toolbar is implemented in JavaFX and these events are no longer captured globally
        tabbedPaneContainer.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // We need to consume a few events that have a global listener
                // Otherwise, they propagate to the JFrame (i.e. "Ctrl + A" in the entry editor still triggers the "Select all" action)
                Optional<KeyBinding> keyBinding = Globals.getKeyPrefs().mapToKeyBinding(e);
                if (keyBinding.isPresent()) {
                    switch (keyBinding.get()) {
                        case CUT:
                        case COPY:
                        case PASTE:
                        case DELETE_ENTRY:
                        case SELECT_ALL:
                            e.consume();
                            break;
                        default:
                            //do nothing
                    }
                }
            }
        });

        SwingNode swingSidePane = new SwingNode();
        SwingUtilities.invokeLater(() -> {
            swingSidePane.setContent(sidePaneManager.getPanel());
        });
        splitPane.getItems().addAll(swingSidePane, tabbedPane);
        setCenter(splitPane);

        UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
        sidePaneManager.updateView();

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints con = new GridBagConstraints();
        con.fill = GridBagConstraints.BOTH;
        con.anchor = GridBagConstraints.WEST;
        JPanel status = new JPanel();
        status.setLayout(gbl);
        con.weighty = 0;
        con.weightx = 0;
        con.gridwidth = 1;
        con.insets = new Insets(0, 2, 0, 0);
        gbl.setConstraints(statusLabel, con);
        status.add(statusLabel);
        con.weightx = 1;
        con.insets = new Insets(0, 4, 0, 0);
        con.gridwidth = 1;
        gbl.setConstraints(statusLine, con);
        status.add(statusLine);
        con.weightx = 0;
        con.gridwidth = GridBagConstraints.REMAINDER;
        con.insets = new Insets(2, 4, 2, 2);
        gbl.setConstraints(progressBar, con);
        status.add(progressBar);
        statusLabel.setForeground(GUIGlobals.ENTRY_EDITOR_LABEL_COLOR.darker());
        //getContentPane().add(status, BorderLayout.PAGE_END);

        // Drag and drop for tabbedPane:
        TransferHandler xfer = new EntryTableTransferHandler(null, this, null);
        // TODO:
        //tabbedPane.setTransferHandler(xfer);
        tlb.setTransferHandler(xfer);
        // TODO: mb.setTransferHandler(xfer);
        sidePaneManager.getPanel().setTransferHandler(xfer);
    }

    /**
     * Returns the indexed BasePanel.
     *
     * @param i Index of base
     */
    public BasePanel getBasePanelAt(int i) {
        return (BasePanel) tabbedPane.getTabs().get(i).getContent();
    }

    /**
     * Returns a list of BasePanel.
     *
     */
    public List<BasePanel> getBasePanelList() {
        List<BasePanel> returnList = new ArrayList<>();
        for (int i = 0; i < getBasePanelCount(); i++) {
            returnList.add(getBasePanelAt(i));
        }
        return returnList;
    }

    public void showBasePanelAt(int i) {
        tabbedPane.getSelectionModel().select(i);
    }

    public void showBasePanel(BasePanel bp) {
        tabbedPane.getSelectionModel().select(getTab(bp));
    }

    /**
     * Returns the currently viewed BasePanel.
     */
    public BasePanel getCurrentBasePanel() {
        if (tabbedPane == null || tabbedPane.getSelectionModel().getSelectedItem() == null) {
            return null;
        }
        return (BasePanel) tabbedPane.getSelectionModel().getSelectedItem().getContent();
    }

    /**
     * @return the BasePanel count.
     */
    public int getBasePanelCount() {
        return tabbedPane.getTabs().size();
    }

    private Tab getTab(BasePanel comp) {
        for (Tab tab : tabbedPane.getTabs()) {
            if (tab.getContent() == comp) {
                return tab;
            }
        }
        return null;
    }

    /**
     * @deprecated do not operate on tabs but on BibDatabaseContexts
     */
    @Deprecated
    public TabPane getTabbedPane() {
        return tabbedPane;
    }

    public void setTabTitle(BasePanel comp, String title, String toolTip) {
        DefaultTaskExecutor.runInJavaFXThread(() -> {
            Tab tab = getTab(comp);
            tab.setText(title);
            tab.setTooltip(new Tooltip(toolTip));
        });
    }

    private MenuBar createMenu() {
        ActionFactory factory = new ActionFactory(Globals.getKeyPrefs());

        Menu file = new Menu(Localization.menuTitle("File"));
        Menu edit = new Menu(Localization.menuTitle("Edit"));
        Menu library = new Menu(Localization.menuTitle("Library"));
        Menu quality = new Menu(Localization.menuTitle("Quality"));
        Menu view = new Menu(Localization.menuTitle("View"));
        Menu tools = new Menu(Localization.menuTitle("Tools"));
        Menu options = new Menu(Localization.menuTitle("Options"));
        Menu help = new Menu(Localization.menuTitle("Help"));

        file.getItems().addAll(
                factory.createMenuItem(ActionsFX.NEW_LIBRARY_BIBTEX, new NewDatabaseAction(this, BibDatabaseMode.BIBTEX)),
                factory.createMenuItem(ActionsFX.NEW_LIBRARY_BIBLATEX, new NewDatabaseAction(this, BibDatabaseMode.BIBLATEX)),
                factory.createMenuItem(ActionsFX.OPEN_LIBRARY, getOpenDatabaseAction()),
                factory.createMenuItem(ActionsFX.save, new OldDatabaseCommandWrapper(Actions.SAVE, this, Globals.stateManager)),
                factory.createMenuItem(ActionsFX.saveAs, new OldDatabaseCommandWrapper(Actions.SAVE_AS, this, Globals.stateManager)),
                factory.createMenuItem(ActionsFX.saveAll, new SaveAllAction(this)),

                factory.createSubMenu(ActionsFX.import_export,
                        factory.createMenuItem(ActionsFX.mergeDatabaseAction, new OldDatabaseCommandWrapper(Actions.MERGE_DATABASE, this, Globals.stateManager)), // TODO: merge with import
                        factory.createMenuItem(ActionsFX.importCurrent, new ImportCommand(this, true)),
                        factory.createMenuItem(ActionsFX.importNew, new ImportCommand(this, false)),
                        factory.createMenuItem(ActionsFX.exportAll, new ExportCommand(this, false)),
                        factory.createMenuItem(ActionsFX.exportSelected, new ExportCommand(this, true)),
                        factory.createMenuItem(ActionsFX.saveSelectedAsPlain, new OldDatabaseCommandWrapper(Actions.SAVE_SELECTED_AS_PLAIN, this, Globals.stateManager))
                ),

                new SeparatorMenuItem(),

                factory.createMenuItem(ActionsFX.connectToSharedDatabaseAction, new ConnectToSharedDatabaseCommand(this)),
                factory.createMenuItem(ActionsFX.pullChangesFromSharedDatabase, new OldDatabaseCommandWrapper(Actions.PULL_CHANGES_FROM_SHARED_DATABASE, this, Globals.stateManager)),

                new SeparatorMenuItem(),

                fileHistory,

                new SeparatorMenuItem(),

                factory.createMenuItem(ActionsFX.closeDatabaseAction, new CloseDatabaseAction()),
                factory.createMenuItem(ActionsFX.quit, new CloseAction())
        );

        edit.getItems().addAll(
                factory.createMenuItem(ActionsFX.undo, new OldDatabaseCommandWrapper(Actions.UNDO, this, Globals.stateManager)),
                factory.createMenuItem(ActionsFX.redo, new OldDatabaseCommandWrapper(Actions.REDO, this, Globals.stateManager)),

                new SeparatorMenuItem(),

                factory.createMenuItem(ActionsFX.CUT, new EditAction(Actions.CUT)),
                factory.createMenuItem(ActionsFX.COPY, new EditAction(Actions.COPY)),
                factory.createSubMenu(ActionsFX.COPY_MORE,
                        factory.createMenuItem(ActionsFX.COPY_TITLE, new OldDatabaseCommandWrapper(Actions.COPY_TITLE, this, Globals.stateManager)),
                        factory.createMenuItem(ActionsFX.COPY_KEY, new OldDatabaseCommandWrapper(Actions.COPY_KEY, this, Globals.stateManager)),
                        factory.createMenuItem(ActionsFX.COPY_CITE_KEY, new OldDatabaseCommandWrapper(Actions.COPY_CITE_KEY, this, Globals.stateManager)),
                        factory.createMenuItem(ActionsFX.COPY_KEY_AND_TITLE, new OldDatabaseCommandWrapper(Actions.COPY_KEY_AND_TITLE, this, Globals.stateManager)),
                        factory.createMenuItem(ActionsFX.COPY_KEY_AND_LINK, new OldDatabaseCommandWrapper(Actions.COPY_KEY_AND_LINK, this, Globals.stateManager)),
                        factory.createMenuItem(ActionsFX.COPY_CITATION_PREVIEW, new OldDatabaseCommandWrapper(Actions.COPY_CITATION_HTML, this, Globals.stateManager)),
                        factory.createMenuItem(ActionsFX.EXPORT_SELECTED_TO_CLIPBOARD, new OldDatabaseCommandWrapper(Actions.EXPORT_TO_CLIPBOARD, this, Globals.stateManager))
                ),

                factory.createMenuItem(ActionsFX.PASTE, new EditAction(Actions.PASTE)),

                new SeparatorMenuItem(),

                factory.createMenuItem(ActionsFX.sendAsEmail, new OldDatabaseCommandWrapper(Actions.SEND_AS_EMAIL, this, Globals.stateManager)),

                new SeparatorMenuItem()
        );
        /*
        edit.add(mark);
        for (int i = 0; i < EntryMarker.MAX_MARKING_LEVEL; i++) {
            markSpecific.add(new MarkEntriesAction(this, i).getMenuItem());
        }
        edit.add(markSpecific);
        edit.add(unmark);
        edit.add(unmarkAll);
        edit.addSeparator();
        */
        /* TODO
        if (Globals.prefs.getBoolean(JabRefPreferences.SPECIALFIELDSENABLED)) {
            boolean menuitem = false;
            if (Globals.prefs.getBoolean(JabRefPreferences.SHOWCOLUMN_RANKING)) {
                rankSubMenu = new JMenu();
                // TODO RightClickMenu.createSpecialFieldMenu(rankSubMenu, SpecialField.RANKING, this);
                edit.add(rankSubMenu);
                menuitem = true;
            }
            if (Globals.prefs.getBoolean(JabRefPreferences.SHOWCOLUMN_RELEVANCE)) {
                edit.add(toggleRelevance);
                menuitem = true;
            }
            if (Globals.prefs.getBoolean(JabRefPreferences.SHOWCOLUMN_QUALITY)) {
                edit.add(toggleQualityAssured);
                menuitem = true;
            }
            if (Globals.prefs.getBoolean(JabRefPreferences.SHOWCOLUMN_PRIORITY)) {
                rankSubMenu = new JMenu();
                // TODO RightClickMenu.createSpecialFieldMenu(rankSubMenu, SpecialField.PRIORITY, this);
                edit.add(rankSubMenu);
                menuitem = true;
            }
            if (Globals.prefs.getBoolean(JabRefPreferences.SHOWCOLUMN_PRINTED)) {
                edit.add(togglePrinted);
                menuitem = true;
            }
            if (Globals.prefs.getBoolean(JabRefPreferences.SHOWCOLUMN_READ)) {
                rankSubMenu = new JMenu();
                // TODO RightClickMenu.createSpecialFieldMenu(rankSubMenu, SpecialField.READ_STATUS, this);
                edit.add(rankSubMenu);
                menuitem = true;
            }
            if (menuitem) {
                edit.addSeparator();
            }
        }
        */

        //factory.createMenuItem(ActionsFX., new OldDatabaseCommandWrapper(Actions., this, Globals.stateManager)),

        edit.getItems().addAll(
                factory.createMenuItem(ActionsFX.manageKeywords, new ManageKeywordsAction(this)),
                factory.createMenuItem(ActionsFX.replaceAll, new OldDatabaseCommandWrapper(Actions.REPLACE_ALL, this, Globals.stateManager)),
                factory.createMenuItem(ActionsFX.massSetField, new MassSetFieldAction(this))
        );

        /*
        search.add(normalSearch);
        search.addSeparator();
        search.add(new JCheckBoxMenuItem(generalFetcher.getToggleAction()));
        if (prefs.getBoolean(JabRefPreferences.WEB_SEARCH_VISIBLE)) {
            sidePaneManager.register(generalFetcher);
            sidePaneManager.show(GeneralFetcher.class);
        }
        mb.add(search);

        groups.add(new JCheckBoxMenuItem(groupSidePane.getToggleAction()));

        groups.addSeparator();
        groups.add(addToGroup);
        groups.add(removeFromGroup);
        groups.add(moveToGroup);
        mb.add(groups);

        view.add(getBackAction());
        view.add(getForwardAction());
        view.add(focusTable);
        view.add(nextTab);
        view.add(prevTab);
        view.add(sortTabs);
        view.addSeparator();
        view.add(increaseFontSize);
        view.add(decreseFontSize);
        view.add(defaultFontSize);
        view.addSeparator();
        view.add(new JCheckBoxMenuItem(toggleToolbar));
        view.add(new JCheckBoxMenuItem(enableToggle(generalFetcher.getToggleAction())));
        view.add(new JCheckBoxMenuItem(groupSidePane.getToggleAction()));
        view.add(new JCheckBoxMenuItem(togglePreview));
        view.add(showPdvViewer);
        view.add(getNextPreviewStyleAction());
        view.add(getPreviousPreviewStyleAction());

        mb.add(view);

        library.add(newEntryAction);

        for (NewEntryAction a : newSpecificEntryAction) {
            newSpec.add(a);
        }
        library.add(newSpec);

        library.add(plainTextImport);
        library.addSeparator();
        library.add(editEntry);
        library.add(editPreamble);
        library.add(editStrings);
        library.addSeparator();
        library.add(customizeAction);
        library.addSeparator();
        library.add(deleteEntry);
        library.add(databaseProperties),
        newSpec = JabRefFrame.subMenu(Localization.menuTitle("New entry by type..."));
        mb.add(library);

        quality.add(dupliCheck);
        quality.add(mergeEntries);
        quality.addSeparator();
        quality.add(resolveDuplicateKeys);
        quality.add(checkIntegrity);
        quality.add(cleanupEntries);
        quality.add(massSetField);
        quality.add(makeKeyAction);
        quality.addSeparator();
        quality.add(autoSetFile);
        quality.add(findUnlinkedFiles);
        quality.add(autoLinkFile);

        for (IdFetcher fetcher : WebFetchers.getIdFetchers(Globals.prefs.getImportFormatPreferences())) {
            lookupIdentifiers.add(new LookupIdentifierAction(this, fetcher));
        }
        quality.add(lookupIdentifiers);
        quality.add(downloadFullText);
        mb.add(quality);

        tools.add(newSubDatabaseAction);
        tools.add(writeXmpAction);
        tools.add(new JCheckBoxMenuItem(openOfficePanel.getToggleAction()));
        tools.add(pushExternalButton.getMenuAction());
        tools.addSeparator();
        tools.add(openFolder);
        tools.add(openFile);
        tools.add(openUrl);
        tools.add(openConsole);
        tools.addSeparator();
        file.add(exportLinkedFiles),

        tools.add(abbreviateIso);
        tools.add(abbreviateMedline);
        tools.add(unabbreviate);
        mb.add(tools);

        options.add(showPrefs);

        AbstractAction genFieldsCustomization = new GenFieldsCustomizationAction();
        AbstractAction protectTerms = new ProtectedTermsAction();
        options.add(genFieldsCustomization);
        options.add(customImpAction);
        options.add(customExpAction);
        options.add(customFileTypesAction);
        options.add(manageJournals);
        options.add(keyBindingAction);
        options.add(protectTerms);
        options.add(manageSelectors);
        mb.add(options);

        help.add(this.help);
        help.add(openForumAction);
        help.addSeparator();
        help.add(errorConsole);
        help.addSeparator();
        help.add(new SearchForUpdateAction());
        JMenu webMenu = JabRefFrame.subMenu(Localization.menuTitle("JabRef resources"));
        webMenu.add(jabrefWebPageAction);
        webMenu.add(jabrefBlogAction);
        webMenu.add(jabrefFacebookAction);
        webMenu.add(jabrefTwitterAction);
        webMenu.addSeparator();
        webMenu.add(forkMeOnGitHubAction);
        webMenu.add(developmentVersionAction);
        webMenu.add(changeLogAction);
        webMenu.addSeparator();
        webMenu.add(donationAction);
        help.add(webMenu);
        help.add(about);
        mb.add(help);

        createDisabledIconsForMenuEntries(mb);
        */

        MenuBar menu = new MenuBar();
        menu.getMenus().addAll(
                file,
                edit,
                library,
                quality,
                view,
                tools,
                options,
                help
        );
        return menu;
    }

    public void addParserResult(ParserResult pr, boolean focusPanel) {
        if (pr.toOpenTab()) {
            // Add the entries to the open tab.
            BasePanel panel = getCurrentBasePanel();
            if (panel == null) {
                // There is no open tab to add to, so we create a new tab:
                addTab(pr.getDatabaseContext(), focusPanel);
            } else {
                List<BibEntry> entries = new ArrayList<>(pr.getDatabase().getEntries());
                addImportedEntries(panel, entries, false);
            }
        } else {
            // only add tab if DB is not already open
            Optional<BasePanel> panel = getBasePanelList().stream()
                    .filter(p -> p.getBibDatabaseContext().getDatabaseFile().equals(pr.getFile())).findFirst();

            if (panel.isPresent()) {
                tabbedPane.getSelectionModel().select(getTab(panel.get()));
            } else {
                addTab(pr.getDatabaseContext(), focusPanel);
            }
        }
    }

    /*
    private void createToolBar() {
        tlb.setBorder(null);
        tlb.setRollover(true);

        tlb.setFloatable(false);
        if (Globals.prefs.getBoolean(JabRefPreferences.BIBLATEX_DEFAULT_MODE)) {
            tlb.addAction(newBiblatexDatabaseAction);
        } else {
            tlb.addAction(newBibtexDatabaseAction);
        }
        tlb.addAction(getOpenDatabaseAction());
        tlb.addAction(save);
        tlb.addAction(saveAll);

        tlb.addSeparator();
        tlb.addAction(cut);
        tlb.addAction(copy);
        tlb.addAction(paste);
        tlb.addAction(undo);
        tlb.addAction(redo);

        tlb.addSeparator();
        tlb.addAction(getBackAction());
        tlb.addAction(getForwardAction());
        tlb.addSeparator();
        tlb.addAction(newEntryAction);
        tlb.addAction(editEntry);
        tlb.addAction(editStrings);
        tlb.addAction(deleteEntry);
        tlb.addSeparator();
        tlb.addAction(makeKeyAction);
        tlb.addAction(cleanupEntries);
        tlb.addAction(mergeEntries);
        tlb.addAction(pullChangesFromSharedDatabase);
        tlb.addAction(openConsole);

        tlb.addSeparator();
        tlb.addAction(mark);
        tlb.addAction(unmark);
        if (Globals.prefs.getBoolean(JabRefPreferences.SPECIALFIELDSENABLED)) {
            if (Globals.prefs.getBoolean(JabRefPreferences.SHOWCOLUMN_RANKING)) {
                JButton button = SpecialFieldDropDown
                        .generateSpecialFieldButtonWithDropDown(SpecialField.RANKING, this);
                tlb.add(button);
                specialFieldButtons.add(button);
            }
            if (Globals.prefs.getBoolean(JabRefPreferences.SHOWCOLUMN_RELEVANCE)) {
                tlb.addAction(toggleRelevance);
            }
            if (Globals.prefs.getBoolean(JabRefPreferences.SHOWCOLUMN_QUALITY)) {
                tlb.addAction(toggleQualityAssured);
            }
            if (Globals.prefs.getBoolean(JabRefPreferences.SHOWCOLUMN_PRIORITY)) {
                JButton button = SpecialFieldDropDown
                        .generateSpecialFieldButtonWithDropDown(SpecialField.PRIORITY, this);
                tlb.add(button);
                specialFieldButtons.add(button);
            }
            if (Globals.prefs.getBoolean(JabRefPreferences.SHOWCOLUMN_PRINTED)) {
                tlb.addAction(togglePrinted);
            }
            if (Globals.prefs.getBoolean(JabRefPreferences.SHOWCOLUMN_READ)) {
                JButton button = SpecialFieldDropDown
                        .generateSpecialFieldButtonWithDropDown(SpecialField.READ_STATUS, this);
                tlb.add(button);
                specialFieldButtons.add(button);
            }
        }
        tlb.addSeparator();

        tlb.addJToggleButton(new JToggleButton(generalFetcher.getToggleAction()));

        previewToggle = new JToggleButton(togglePreview);
        tlb.addJToggleButton(previewToggle);

        tlb.addJToggleButton(new JToggleButton(groupSidePane.getToggleAction()));

        tlb.addSeparator();

        tlb.add(pushExternalButton.getComponent());
        tlb.addSeparator();
        tlb.add(donationAction);
        tlb.add(forkMeOnGitHubAction);
        tlb.add(jabrefFacebookAction);
        tlb.add(jabrefTwitterAction);

        createDisabledIconsForButtons(tlb);
    }
    */

    /**
     * displays the String on the Status Line visible on the bottom of the JabRef mainframe
     */
    public void output(final String s) {
        SwingUtilities.invokeLater(() -> {
            statusLine.setText(s);
            statusLine.repaint();
        });
    }

    private void initActions() {
        /*
        openDatabaseOnlyActions.clear();
        openDatabaseOnlyActions.addAll(Arrays.asList(manageSelectors, mergeDatabaseAction, newSubDatabaseAction, save, copyPreview,
                saveAs, saveSelectedAs, saveSelectedAsPlain, undo, redo, cut, deleteEntry, copy, paste, mark, markSpecific, unmark,
                unmarkAll, rankSubMenu, editEntry, selectAll, copyKey, copyCiteKey, copyKeyAndTitle, copyKeyAndLink, editPreamble, editStrings,
                groupSidePane.getToggleAction(), makeKeyAction, normalSearch, generalFetcher.getToggleAction(), mergeEntries, cleanupEntries, exportToClipboard, replaceAll,
                sendAsEmail, downloadFullText, lookupIdentifiers, writeXmpAction, openOfficePanel.getToggleAction(), findUnlinkedFiles, addToGroup, removeFromGroup,
                moveToGroup, autoLinkFile, resolveDuplicateKeys, openUrl, openFolder, openFile, togglePreview,
                dupliCheck, autoSetFile, newEntryAction, newSpec, customizeAction, plainTextImport, getMassSetField(), getManageKeywords(),
                pushExternalButton.getMenuAction(), closeDatabaseAction, getNextPreviewStyleAction(), getPreviousPreviewStyleAction(), checkIntegrity,
                databaseProperties, abbreviateIso, abbreviateMedline,
                unabbreviate, exportAll, exportSelected, importCurrent, saveAll, focusTable, increaseFontSize, decreseFontSize, defaultFontSize,
                toggleRelevance, toggleQualityAssured, togglePrinted, pushExternalButton.getComponent()));

        openDatabaseOnlyActions.addAll(newSpecificEntryAction);

        openDatabaseOnlyActions.addAll(specialFieldButtons);

        severalDatabasesOnlyActions.clear();
        severalDatabasesOnlyActions.addAll(Arrays
                .asList(nextTab, prevTab, sortTabs));

        openAndSavedDatabasesOnlyActions.addAll(Collections.singletonList(openConsole));
        sharedDatabaseOnlyActions.addAll(Collections.singletonList(pullChangesFromSharedDatabase));
        noSharedDatabaseActions.addAll(Arrays.asList(save, saveAll));

        oneEntryOnlyActions.clear();
        oneEntryOnlyActions.addAll(Arrays.asList(editEntry));

        oneEntryWithFileOnlyActions.clear();
        oneEntryWithFileOnlyActions.addAll(Arrays.asList(openFolder, openFile));

        oneEntryWithURLorDOIOnlyActions.clear();
        oneEntryWithURLorDOIOnlyActions.addAll(Arrays.asList(openUrl));

        twoEntriesOnlyActions.clear();
        twoEntriesOnlyActions.addAll(Arrays.asList(mergeEntries));

        atLeastOneEntryActions.clear();
        atLeastOneEntryActions.addAll(Arrays.asList(downloadFullText, lookupIdentifiers, exportLinkedFiles));

        tabbedPane.getTabs().addListener(this::updateEnabledState);
        */
    }

    /**
     * Enable or Disable all actions based on the number of open tabs.
     * <p>
     * The action that are affected are set in initActions.
     */
    public void updateEnabledState(ListChangeListener.Change<? extends Tab> change) {
        int tabCount = tabbedPane.getTabs().size();
        if (!change.next()) {
            return;
        }
        if (change.wasAdded() || change.wasRemoved()) {
            setEnabled(openDatabaseOnlyActions, tabCount > 0);
            setEnabled(severalDatabasesOnlyActions, tabCount > 1);
        }
        if (tabCount == 0) {
            getBackAction().setEnabled(false);
            getForwardAction().setEnabled(false);
            setEnabled(openAndSavedDatabasesOnlyActions, false);
            setEnabled(sharedDatabaseOnlyActions, false);
            setEnabled(oneEntryOnlyActions, false);
        }

        if (tabCount > 0) {
            BasePanel current = getCurrentBasePanel();
            boolean saved = current.getBibDatabaseContext().getDatabasePath().isPresent();
            setEnabled(openAndSavedDatabasesOnlyActions, saved);

            boolean isShared = current.getBibDatabaseContext().getLocation() == DatabaseLocation.SHARED;
            setEnabled(sharedDatabaseOnlyActions, isShared);
            setEnabled(noSharedDatabaseActions, !isShared);

            boolean oneEntrySelected = current.getSelectedEntries().size() == 1;
            setEnabled(oneEntryOnlyActions, oneEntrySelected);
            setEnabled(oneEntryWithFileOnlyActions, isExistFile(current.getSelectedEntries()));
            setEnabled(oneEntryWithURLorDOIOnlyActions, isExistURLorDOI(current.getSelectedEntries()));

            boolean twoEntriesSelected = current.getSelectedEntries().size() == 2;
            setEnabled(twoEntriesOnlyActions, twoEntriesSelected);

            boolean atLeastOneEntrySelected = !current.getSelectedEntries().isEmpty();
            setEnabled(atLeastOneEntryActions, atLeastOneEntrySelected);
        }
    }

    /**
     * This method causes all open BasePanels to set up their tables
     * anew. When called from PrefsDialog3, this updates to the new
     * settings.
     */
    public void setupAllTables() {
        // This action can be invoked without an open database, so
        // we have to check if we have one before trying to invoke
        // methods to execute changes in the preferences.

        // We want to notify all tabs about the changes to
        // avoid problems when changing the column set.
        for (int i = 0; i < tabbedPane.getTabs().size(); i++) {
            BasePanel bf = getBasePanelAt(i);

            // Update tables:
            if (bf.getDatabase() != null) {
                DefaultTaskExecutor.runInJavaFXThread(bf::setupMainPanel);
            }
        }
    }

    private List<String> collectDatabaseFilePaths() {
        List<String> dbPaths = new ArrayList<>(getBasePanelCount());

        for (BasePanel basePanel : getBasePanelList()) {
            try {
                // db file exists
                if (basePanel.getBibDatabaseContext().getDatabaseFile().isPresent()) {
                    dbPaths.add(basePanel.getBibDatabaseContext().getDatabaseFile().get().getCanonicalPath());
                } else {
                    dbPaths.add("");
                }
            } catch (IOException ex) {
                LOGGER.error("Invalid database file path: " + ex.getMessage());
            }
        }
        return dbPaths;
    }

    private List<String> getUniquePathParts() {
        List<String> dbPaths = collectDatabaseFilePaths();

        return FileUtil.uniquePathSubstrings(dbPaths);
    }

    public void updateAllTabTitles() {
        List<String> paths = getUniquePathParts();
        for (int i = 0; i < getBasePanelCount(); i++) {
            String uniqPath = paths.get(i);
            Optional<File> file = getBasePanelAt(i).getBibDatabaseContext().getDatabaseFile();

            if (file.isPresent()) {
                if (!uniqPath.equals(file.get().getName()) && uniqPath.contains(File.separator)) {
                    // remove filename
                    uniqPath = uniqPath.substring(0, uniqPath.lastIndexOf(File.separator));
                    tabbedPane.getTabs().get(i).setText(getBasePanelAt(i).getTabTitle() + " \u2014 " + uniqPath);
                } else {
                    // set original filename (again)
                    tabbedPane.getTabs().get(i).setText(getBasePanelAt(i).getTabTitle());
                }
            } else {
                tabbedPane.getTabs().get(i).setText(getBasePanelAt(i).getTabTitle());
            }
            tabbedPane.getTabs().get(i).setTooltip(new Tooltip(file.map(File::getAbsolutePath).orElse(null)));
        }
    }

    public void addTab(BasePanel basePanel, boolean raisePanel) {
        DefaultTaskExecutor.runInJavaFXThread(() -> {
            // add tab
            Tab newTab = new Tab(basePanel.getTabTitle(), basePanel);
            tabbedPane.getTabs().add(newTab);

            // update all tab titles
            updateAllTabTitles();

            if (raisePanel) {
                tabbedPane.getSelectionModel().select(newTab);
            }

            // Register undo/redo listener
            basePanel.getUndoManager().registerListener(new UndoRedoEventManager());

            BibDatabaseContext context = basePanel.getBibDatabaseContext();

            if (readyForAutosave(context)) {
                AutosaveManager autosaver = AutosaveManager.start(context);
                autosaver.registerListener(new AutosaveUIManager(basePanel));
            }

            BackupManager.start(context);

            // Track opening
            trackOpenNewDatabase(basePanel);
        });
    }

    private void trackOpenNewDatabase(BasePanel basePanel) {

        Map<String, String> properties = new HashMap<>();
        Map<String, Double> measurements = new HashMap<>();
        measurements.put("NumberOfEntries", (double)basePanel.getDatabaseContext().getDatabase().getEntryCount());

        Globals.getTelemetryClient().ifPresent(client -> client.trackEvent("OpenNewDatabase", properties, measurements));
    }

    public BasePanel addTab(BibDatabaseContext databaseContext, boolean raisePanel) {
        Objects.requireNonNull(databaseContext);

        BasePanel bp = new BasePanel(this, BasePanelPreferences.from(Globals.prefs), databaseContext, ExternalFileTypes.getInstance());
        addTab(bp, raisePanel);
        return bp;
    }

    private boolean readyForAutosave(BibDatabaseContext context) {
        return ((context.getLocation() == DatabaseLocation.SHARED) ||
                ((context.getLocation() == DatabaseLocation.LOCAL) && Globals.prefs.getBoolean(JabRefPreferences.LOCAL_AUTO_SAVE))) &&
                context.getDatabaseFile().isPresent();
    }

    /**
     * Creates icons for the disabled state for all JMenuItems with FontBasedIcons in the given menuElement.
     * This is necessary as Swing is not able to generate default disabled icons for font based icons.
     *
     * @param menuElement the menuElement for which disabled icons should be generated
     */
    public void createDisabledIconsForMenuEntries(MenuElement menuElement) {
        for (MenuElement subElement : menuElement.getSubElements()) {
            if ((subElement instanceof JMenu) || (subElement instanceof JPopupMenu)) {
                createDisabledIconsForMenuEntries(subElement);
            } else if (subElement instanceof JMenuItem) {
                JMenuItem item = (JMenuItem) subElement;
                if (item.getIcon() instanceof IconTheme.FontBasedIcon) {
                    item.setDisabledIcon(((IconTheme.FontBasedIcon) item.getIcon()).createDisabledIcon());
                }
            }
        }
    }

    public void createDisabledIconsForButtons(Container container) {
        for (int index = 0; index < container.getComponentCount(); index++) {
            Component component = container.getComponent(index);
            if (component instanceof JButton) {
                JButton button = (JButton) component;
                if (button.getIcon() instanceof IconTheme.FontBasedIcon) {
                    button.setDisabledIcon(((IconTheme.FontBasedIcon) button.getIcon()).createDisabledIcon());
                }
            } else if (component instanceof JPanel) {
                createDisabledIconsForButtons((JPanel) component);
            }
        }
    }

    public void sortTabs() {
        // We are going to be crude: remove all tabs and re-add them in a sorted way
        // This is ugly, but otherwise we run into issues: https://stackoverflow.com/questions/37328760/javafx-tabpane-sorting-tabs-creates-havoc
        List<Tab> tabs = new ArrayList<>(tabbedPane.getTabs());
        tabs.sort((o1, o2) -> o2.getText().compareTo(o1.getText()));
        tabbedPane.getTabs().clear();
        tabbedPane.getTabs().setAll(tabs);
    }

    /**
     * This method does the job of adding imported entries into the active
     * database, or into a new one. It shows the ImportInspectionDialog if
     * preferences indicate it should be used. Otherwise it imports directly.
     *
     * @param panel     The BasePanel to add to.
     * @param entries   The entries to add.
     * @param openInNew Should the entries be imported into a new database?
     */
    private void addImportedEntries(final BasePanel panel, final List<BibEntry> entries, final boolean openInNew) {
        SwingUtilities.invokeLater(() -> {
            ImportInspectionDialog diag = new ImportInspectionDialog(JabRefFrame.this, panel,
                    Localization.lang("Import"), openInNew);
            diag.addEntries(entries);
            diag.entryListComplete();
            //diag.setLocationRelativeTo(JabRefFrame.this);
            diag.setVisible(true);
            diag.toFront();
        });
    }

    public FileHistoryMenu getFileHistory() {
        return fileHistory;
    }

    /**
     * This method shows a wait cursor and blocks all input to the JFrame's contents.
     */
    public void block() {
        changeBlocking(true);
    }

    /**
     * This method reverts the cursor to normal, and stops blocking input to the JFrame's contents.
     * There are no adverse effects of calling this method redundantly.
     */
    public void unblock() {
        changeBlocking(false);
    }

    /**
     * Do the actual blocking/unblocking
     *
     * @param blocked true if input should be blocked
     */
    private void changeBlocking(boolean blocked) {
        /*
        if (SwingUtilities.isEventDispatchThread()) {
            getGlassPane().setVisible(blocked);
        } else {
            try {
                SwingUtilities.invokeAndWait(() -> getGlassPane().setVisible(blocked));
            } catch (InvocationTargetException | InterruptedException e) {
                LOGGER.error("Problem " + (blocked ? "" : "un") + "blocking UI", e);
            }
        }
        */
    }

    /**
     * Set the visibility of the progress bar in the right end of the
     * status line at the bottom of the frame.
     * <p>
     * If not called on the event dispatch thread, this method uses
     * SwingUtilities.invokeLater() to do the actual operation on the EDT.
     */
    public void setProgressBarVisible(final boolean visible) {
        if (SwingUtilities.isEventDispatchThread()) {
            progressBar.setVisible(visible);
        } else {
            SwingUtilities.invokeLater(() -> progressBar.setVisible(visible));
        }
    }

    /**
     * Sets the current value of the progress bar.
     * <p>
     * If not called on the event dispatch thread, this method uses
     * SwingUtilities.invokeLater() to do the actual operation on the EDT.
     */
    public void setProgressBarValue(final int value) {
        if (SwingUtilities.isEventDispatchThread()) {
            progressBar.setValue(value);
        } else {
            SwingUtilities.invokeLater(() -> progressBar.setValue(value));
        }

    }

    /**
     * Sets the indeterminate status of the progress bar.
     * <p>
     * If not called on the event dispatch thread, this method uses
     * SwingUtilities.invokeLater() to do the actual operation on the EDT.
     */
    public void setProgressBarIndeterminate(final boolean value) {
        if (SwingUtilities.isEventDispatchThread()) {
            progressBar.setIndeterminate(value);
        } else {
            SwingUtilities.invokeLater(() -> progressBar.setIndeterminate(value));
        }

    }

    /**
     * Sets the maximum value of the progress bar. Always call this method
     * before using the progress bar, to set a maximum value appropriate to
     * the task at hand.
     * <p>
     * If not called on the event dispatch thread, this method uses
     * SwingUtilities.invokeLater() to do the actual operation on the EDT.
     */
    public void setProgressBarMaximum(final int value) {
        if (SwingUtilities.isEventDispatchThread()) {
            progressBar.setMaximum(value);
        } else {
            SwingUtilities.invokeLater(() -> progressBar.setMaximum(value));
        }

    }

    /**
     * Return a boolean, if the selected entry have file
     * @param selectEntryList A selected entries list of the current base pane
     * @return true, if the selected entry contains file.
     * false, if multiple entries are selected or the selected entry doesn't contains file
     */
    private boolean isExistFile(List<BibEntry> selectEntryList) {
        if (selectEntryList.size() == 1) {
            BibEntry selectedEntry = selectEntryList.get(0);
            return selectedEntry.getField(FieldName.FILE).isPresent();
        }
        return false;
    }

    /**
     * Return a boolean, if the selected entry have url or doi
     * @param selectEntryList A selected entries list of the current base pane
     * @return true, if the selected entry contains url or doi.
     * false, if multiple entries are selected or the selected entry doesn't contains url or doi
     */
    private boolean isExistURLorDOI(List<BibEntry> selectEntryList) {
        if (selectEntryList.size() == 1) {
            BibEntry selectedEntry = selectEntryList.get(0);
            return (selectedEntry.getField(FieldName.URL).isPresent() || selectedEntry.getField(FieldName.DOI).isPresent());
        }
        return false;
    }

    @Override
    public void showMessage(String message, String title, int msgType) {
        JOptionPane.showMessageDialog(null, message, title, msgType);
    }

    @Override
    public void setStatus(String s) {
        output(s);
    }

    @Override
    public void showMessage(String message) {
        JOptionPane.showMessageDialog(null, message);
    }

    private int showSaveDialog(String filename) {
        Object[] options = {Localization.lang("Save changes"),
                Localization.lang("Discard changes"),
                Localization.lang("Return to JabRef")};

        return JOptionPane.showOptionDialog(null,
                Localization.lang("Library '%0' has changed.", filename),
                Localization.lang("Save before closing"), JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE, null, options, options[2]);
    }

    private void closeTab(Tab tab) {
        closeTab(getBasePanel(tab));
    }

    private BasePanel getBasePanel(Tab tab) {
        return (BasePanel) tab.getContent();
    }

    private void closeTab(BasePanel panel) {
        // empty tab without database
        if (panel == null) {
            return;
        }

        BibDatabaseContext context = panel.getBibDatabaseContext();

        if (panel.isModified() && (context.getLocation() == DatabaseLocation.LOCAL)) {
            if (confirmClose(panel)) {
                removeTab(panel);
            }
        } else if (context.getLocation() == DatabaseLocation.SHARED) {
            context.convertToLocalDatabase();
            context.getDBMSSynchronizer().closeSharedDatabase();
            context.clearDBMSSynchronizer();
            removeTab(panel);
        } else {
            removeTab(panel);
        }
        AutosaveManager.shutdown(context);
        BackupManager.shutdown(context);
    }

    // Ask if the user really wants to close, if the base has not been saved
    private boolean confirmClose(BasePanel panel) {
        boolean close = false;
        String filename;

        filename = panel.getBibDatabaseContext().getDatabaseFile().map(File::getAbsolutePath)
                .orElse(GUIGlobals.UNTITLED_TITLE);

        int answer = showSaveDialog(filename);
        if (answer == JOptionPane.YES_OPTION) {
            // The user wants to save.
            try {
                SaveDatabaseAction saveAction = new SaveDatabaseAction(panel);
                saveAction.runCommand();
                if (saveAction.isSuccess()) {
                    close = true;
                }
            } catch (Throwable ex) {
                // do not close
            }
        } else if (answer == JOptionPane.NO_OPTION) {
            // discard changes
            close = true;
        }
        return close;
    }

    private void removeTab(BasePanel panel) {
        panel.cleanUp();
        tabbedPane.getTabs().remove(getTab(panel));
        setWindowTitle();
        output(Localization.lang("Closed library") + '.');
        // update tab titles
        updateAllTabTitles();
    }

    public void closeCurrentTab() {
        removeTab(getCurrentBasePanel());
    }

    public OpenDatabaseAction getOpenDatabaseAction() {
        return new OpenDatabaseAction(this);
    }

    public String getStatusLineText() {
        return statusLine.getText();
    }

    public AbstractAction getForwardAction() {
        return forward;
    }

    public AbstractAction getBackAction() {
        return back;
    }

    public AbstractAction getNextPreviewStyleAction() {
        return nextPreviewStyle;
    }

    public AbstractAction getPreviousPreviewStyleAction() {
        return previousPreviewStyle;
    }

    public SplitPane getSplitPane() {
        return splitPane;
    }

    public SidePaneManager getSidePaneManager() {
        return sidePaneManager;
    }

    public void setPreviewToggle(boolean enabled) {
        previewToggle.setSelected(enabled);
    }

    public PushToApplications getPushApplications() {
        return pushApplications;
    }

    public GlobalSearchBar getGlobalSearchBar() {
        return globalSearchBar;
    }

    public CountingUndoManager getUndoManager() {
        return undoManager;
    }

    private static class MyGlassPane extends JPanel {
        public MyGlassPane() {
            addKeyListener(new KeyAdapter() {
                // Nothing
            });
            addMouseListener(new MouseAdapter() {
                // Nothing
            });
            /*  infoLabel.setForeground(new Color(255, 100, 100, 124));

              setLayout(new BorderLayout());
              add(infoLabel, BorderLayout.CENTER);*/
            super.setCursor(
                    Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }

        // Override isOpaque() to prevent the glasspane from hiding the window contents:
        @Override
        public boolean isOpaque() {
            return false;
        }
    }

    private class GeneralAction extends MnemonicAwareAction {

        private final Actions command;

        public GeneralAction(Actions command, String text) {
            this.command = command;
            putValue(Action.NAME, text);
        }

        public GeneralAction(Actions command, String text, String description) {
            this.command = command;
            putValue(Action.NAME, text);
            putValue(Action.SHORT_DESCRIPTION, description);
        }

        public GeneralAction(Actions command, String text, Icon icon) {
            super(icon);

            this.command = command;
            putValue(Action.NAME, text);
        }

        public GeneralAction(Actions command, String text, String description, Icon icon) {
            super(icon);

            this.command = command;
            putValue(Action.NAME, text);
            putValue(Action.SHORT_DESCRIPTION, description);
        }

        public GeneralAction(Actions command, String text, KeyStroke key) {
            this.command = command;
            putValue(Action.NAME, text);
            putValue(Action.ACCELERATOR_KEY, key);
        }

        public GeneralAction(Actions command, String text, String description, KeyStroke key) {
            this.command = command;
            putValue(Action.NAME, text);
            putValue(Action.SHORT_DESCRIPTION, description);
            putValue(Action.ACCELERATOR_KEY, key);
        }

        public GeneralAction(Actions command, String text, String description, KeyStroke key, Icon icon) {
            super(icon);

            this.command = command;
            putValue(Action.NAME, text);
            putValue(Action.SHORT_DESCRIPTION, description);
            putValue(Action.ACCELERATOR_KEY, key);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (tabbedPane.getTabs().size() > 0) {
                try {
                    getCurrentBasePanel().runCommand(command);
                } catch (Throwable ex) {
                    LOGGER.error("Problem with executing command: " + command, ex);
                }
            } else {
                LOGGER.info("Action '" + command + "' must be disabled when no database is open.");
            }
        }
    }

    /**
     * The action concerned with closing the window.
     */
    private class CloseAction extends SimpleCommand {

        @Override
        public void execute() {
            quit();
            Platform.exit();
        }
    }

    private class ShowPrefsAction extends MnemonicAwareAction {

        public ShowPrefsAction() {
            super(IconTheme.JabRefIcons.PREFERENCES.getIcon());
            putValue(Action.NAME, Localization.menuTitle("Preferences"));
            putValue(Action.SHORT_DESCRIPTION, Localization.lang("Preferences"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            showPreferencesDialog();
        }
    }

    private class ChangeTabAction extends MnemonicAwareAction {

        private final boolean next;

        public ChangeTabAction(boolean next) {
            putValue(Action.NAME, next ? Localization.menuTitle("Next tab") :
                    Localization.menuTitle("Previous tab"));
            this.next = next;
            putValue(Action.ACCELERATOR_KEY,
                    next ? Globals.getKeyPrefs().getKey(KeyBinding.NEXT_TAB) : Globals.getKeyPrefs().getKey(KeyBinding.PREVIOUS_TAB));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (next) {
                tabbedPane.getSelectionModel().selectNext();
            } else {
                tabbedPane.getSelectionModel().selectPrevious();
            }
        }
    }

    /**
     * Class for handling general actions; cut, copy and paste. The focused component is
     * kept track of by Globals.focusListener, and we call the action stored under the
     * relevant name in its action map.
     */
    private class EditAction extends SimpleCommand {

        private final Actions command;

        public EditAction(Actions command) {
            this.command = command;
        }

        @Override
        public void execute() {
            LOGGER.debug(Globals.getFocusListener().getFocused().toString());
            JComponent source = Globals.getFocusListener().getFocused();
            Action action = source.getActionMap().get(command);
            if (action != null) {
                action.actionPerformed(new ActionEvent(source, 0, command.name()));
            }
        }
    }

    private class CustomizeExportsAction extends MnemonicAwareAction {

        public CustomizeExportsAction() {
            putValue(Action.NAME, Localization.menuTitle("Manage custom exports"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ExportCustomizationDialog ecd = new ExportCustomizationDialog(JabRefFrame.this);
            ecd.setVisible(true);
        }
    }

    private class CustomizeImportsAction extends MnemonicAwareAction {

        public CustomizeImportsAction() {
            putValue(Action.NAME, Localization.menuTitle("Manage custom imports"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ImportCustomizationDialog ecd = new ImportCustomizationDialog(JabRefFrame.this);
            ecd.setVisible(true);
        }
    }

    private class CustomizeEntryTypeAction extends MnemonicAwareAction {

        public CustomizeEntryTypeAction() {
            putValue(Action.NAME, Localization.menuTitle("Customize entry types"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JDialog dl = new EntryCustomizationDialog(JabRefFrame.this);
            dl.setVisible(true);
        }
    }

    private class GenFieldsCustomizationAction extends MnemonicAwareAction {

        public GenFieldsCustomizationAction() {
            putValue(Action.NAME, Localization.menuTitle("Set up general fields"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            GenFieldsCustomizer gf = new GenFieldsCustomizer(JabRefFrame.this);
            gf.setVisible(true);

        }
    }

    private class ProtectedTermsAction extends MnemonicAwareAction {

        public ProtectedTermsAction() {
            putValue(Action.NAME, Localization.menuTitle("Manage protected terms"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ProtectedTermsDialog protectTermsDialog = new ProtectedTermsDialog(JabRefFrame.this,
                    Globals.protectedTermsLoader);
            protectTermsDialog.setVisible(true);
        }
    }

    private class DatabasePropertiesAction extends MnemonicAwareAction {

        private DatabasePropertiesDialog propertiesDialog;

        public DatabasePropertiesAction() {
            putValue(Action.NAME, Localization.menuTitle("Library properties"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (propertiesDialog == null) {
                propertiesDialog = new DatabasePropertiesDialog(null);
            }
            propertiesDialog.setPanel(getCurrentBasePanel());
            propertiesDialog.updateEnableStatus();
            propertiesDialog.setLocationRelativeTo(null);
            propertiesDialog.setVisible(true);
        }

    }

    private class BibtexKeyPatternAction extends MnemonicAwareAction {

        private BibtexKeyPatternDialog bibtexKeyPatternDialog;

        public BibtexKeyPatternAction() {
            putValue(Action.NAME, Localization.lang("BibTeX key patterns"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JabRefPreferences.getInstance();
            if (bibtexKeyPatternDialog == null) {
                // if no instance of BibtexKeyPatternDialog exists, create new one
                bibtexKeyPatternDialog = new BibtexKeyPatternDialog(JabRefFrame.this, getCurrentBasePanel());
            } else {
                // BibtexKeyPatternDialog allows for updating content based on currently selected panel
                bibtexKeyPatternDialog.setPanel(getCurrentBasePanel());
            }
            bibtexKeyPatternDialog.setLocationRelativeTo(null);
            bibtexKeyPatternDialog.setVisible(true);
        }

    }

    private class DefaultTableFontSizeAction extends MnemonicAwareAction {

        public DefaultTableFontSizeAction() {
            putValue(Action.NAME, Localization.menuTitle("Default table font size"));
            putValue(Action.ACCELERATOR_KEY, Globals.getKeyPrefs().getKey(KeyBinding.DEFAULT_TABLE_FONT_SIZE));
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            GUIGlobals.setFont(Globals.prefs.getIntDefault(JabRefPreferences.FONT_SIZE));
            for (BasePanel basePanel : getBasePanelList()) {
                basePanel.updateTableFont();
            }
            setStatus(Localization.lang("Table font size is %0", String.valueOf(GUIGlobals.currentFont.getSize())));
        }
    }

    private class IncreaseTableFontSizeAction extends MnemonicAwareAction {

        public IncreaseTableFontSizeAction() {
            putValue(Action.NAME, Localization.menuTitle("Increase table font size"));
            putValue(Action.ACCELERATOR_KEY, Globals.getKeyPrefs().getKey(KeyBinding.INCREASE_TABLE_FONT_SIZE));
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            GUIGlobals.setFont(GUIGlobals.currentFont.getSize() + 1);
            for (BasePanel basePanel : getBasePanelList()) {
                basePanel.updateTableFont();
            }
            setStatus(Localization.lang("Table font size is %0", String.valueOf(GUIGlobals.currentFont.getSize())));
        }
    }

    private class DecreaseTableFontSizeAction extends MnemonicAwareAction {

        public DecreaseTableFontSizeAction() {
            putValue(Action.NAME, Localization.menuTitle("Decrease table font size"));
            putValue(Action.ACCELERATOR_KEY, Globals.getKeyPrefs().getKey(KeyBinding.DECREASE_TABLE_FONT_SIZE));
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            int currentSize = GUIGlobals.currentFont.getSize();
            if (currentSize < 2) {
                return;
            }
            GUIGlobals.setFont(currentSize - 1);
            for (BasePanel basePanel : getBasePanelList()) {
                basePanel.updateTableFont();
            }
            setStatus(Localization.lang("Table font size is %0", String.valueOf(GUIGlobals.currentFont.getSize())));
        }
    }

    private class CloseDatabaseAction extends SimpleCommand {

        @Override
        public void execute() {
            closeTab(getCurrentBasePanel());
        }
    }

    private class CloseAllDatabasesAction extends MnemonicAwareAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            final List<Tab> tabs = tabbedPane.getTabs();

            for (Tab tab : tabs) {
                closeTab(tab);
            }
        }
    }

    private class CloseOtherDatabasesAction extends MnemonicAwareAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            final BasePanel active = getCurrentBasePanel();
            final List<Tab> tabs = tabbedPane.getTabs();

            for (Tab tab : tabs) {
                if (!tab.getContent().equals(active)) {
                    closeTab(tab);
                }
            }
        }
    }

    private class ToolBar extends OSXCompatibleToolbar {

        public void addAction(Action a) {
            JButton b = new JButton(a);
            b.setText(null);
            if (!OS.OS_X) {
                b.setMargin(marg);
            }
            // create a disabled Icon for FontBasedIcons as Swing does not automatically create one
            Object obj = a.getValue(Action.LARGE_ICON_KEY);
            if (obj instanceof IconTheme.FontBasedIcon) {
                b.setDisabledIcon(((IconTheme.FontBasedIcon) obj).createDisabledIcon());
            }
            add(b);
        }

        public void addJToggleButton(JToggleButton button) {
            button.setText(null);
            if (!OS.OS_X) {
                button.setMargin(marg);
            }
            Object obj = button.getAction().getValue(Action.LARGE_ICON_KEY);
            if (obj instanceof IconTheme.FontBasedIcon) {
                button.setDisabledIcon(((IconTheme.FontBasedIcon) obj).createDisabledIcon());
            }
            add(button);
        }
    }

    private class UndoRedoEventManager {

        @Subscribe
        public void listen(UndoRedoEvent event) {
            updateTexts(event);
            JabRefFrame.this.getCurrentBasePanel().updateEntryEditorIfShowing();
        }

        @Subscribe
        public void listen(AddUndoableActionEvent event) {
            updateTexts(event);
        }

        private void updateTexts(UndoChangeEvent event) {
            /* TODO
            SwingUtilities.invokeLater(() -> {
                undo.putValue(Action.SHORT_DESCRIPTION, event.getUndoDescription());
                undo.setEnabled(event.isCanUndo());
                redo.putValue(Action.SHORT_DESCRIPTION, event.getRedoDescription());
                redo.setEnabled(event.isCanRedo());
            });
            */
        }
    }
}