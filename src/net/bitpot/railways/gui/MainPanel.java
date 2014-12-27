package net.bitpot.railways.gui;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.ui.treeStructure.Tree;
import net.bitpot.railways.actions.UpdateRoutesListAction;
import net.bitpot.railways.models.*;
import net.bitpot.railways.models.routes.SimpleRoute;
import net.bitpot.railways.parser.RailsRoutesParser;
import net.bitpot.railways.parser.RouteTreeBuilder;
import net.bitpot.railways.routesView.RoutesManager;
import net.bitpot.railways.routesView.RoutesView;
import net.bitpot.railways.routesView.RoutesViewPane;
import net.bitpot.railways.utils.RailwaysUtils;
import org.antlr.v4.runtime.misc.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 *
 */
public class MainPanel {

    @SuppressWarnings("unused")
    private static Logger log = Logger.getInstance(MainPanel.class.getName());

    // Names of cards for main panel that contains several pages.
    // Names should be the same as specified in GUI designer for appropriate panels.
    // Main page with routes table.
    private final static String ROUTES_PANEL_NAME = "routesCard";

    // Panel with message/error information.
    private final static String INFO_PANEL_NAME = "infoCard";


    private final static String NO_INFO = "-";

    private static final int LINK_OPEN_SETTINGS = 1;
    private static final int LINK_SHOW_STACKTRACE = 2;


    private RouteTableModel myTableModel;

    private JPanel rootPanel;
    private JBTable routesTable;
    private JTextField pathFilterField;
    private JPanel centerPanel;
    private HyperlinkLabel infoLink;
    private JLabel infoLbl;
    private JLabel routesCounterLbl;


    private JPanel routeInfoPanel;
    private HyperlinkLabel actionLbl;
    private JLabel nameLbl;
    private JLabel methodLbl;
    private JLabel routeLbl;
    private JPanel topPanel;
    private JPanel actionsPanel;
    private JBScrollPane routesScrollPane;
    private JPanel routesPanel;
    private JPanel routesErrorPanel;
    private JPanel routesTreeviewPanel;
    private JTree routesTree;
    private JPanel routesTablePanel;
    private JPanel routeViews;
    private JBLabel environmentLbl;


    private Project project;

    // A pane that is used as a data source for the Routes panel.
    private RoutesViewPane myDataSource = null;

    // Contains route which information is shown in the info panel.
    // Contains null if no route is selected.
    @Nullable
    private Route currentRoute;

    private int infoLinkAction;
    private String currentPanel;

    private JBSplitter mySplitter;


    public MainPanel(Project project) {
        this.project = project;

        initToolbar();

        // Init handlers after everything is initialized
        initHandlers();

        getRouteTableModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                updateCounterLabel();
            }
        });

        // Init routes tree
        routesTree.setCellRenderer(new RouteTreeCellRenderer());

        updateCounterLabel();

        // Update route info panel
        showRouteInfo(null);

        initSplitter();
        showPanel(ROUTES_PANEL_NAME);
    }


    private void initHandlers() {
        // When filter field text is changed, routes table will be refiltered.
        pathFilterField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent e) {
                getRouteTableModel().getFilter().
                        setPathFilter(pathFilterField.getText());
            }
        });


        infoLink.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                RoutesManager rm = RoutesView.getInstance(project).getCurrentRoutesManager();
                if (rm == null)
                    return;

                switch (infoLinkAction) {
                    case LINK_OPEN_SETTINGS:
                        RailwaysUtils.invokeAction("Railways.settingsAction", project);
                        break;

                    default:
                        RailwaysUtils.showErrorInfo(rm);
                }

            }
        });


        routesTree.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    Tree target = (Tree) e.getSource();
                    navigateToRouteNode((RouteNode) target.getLastSelectedPathComponent());
                }
            }
        });


        // Register mouse handler to handle double-clicks.
        // Double clicking a row will navigate to the action of selected route.
        routesTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Tree target = (Tree) e.getSource();
                    TreePath path = target.getClosestPathForLocation(e.getX(), e.getY());
                    navigateToRouteNode((RouteNode) path.getLastPathComponent());
                }
            }
        });

        actionLbl.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (currentRoute != null)
                    currentRoute.navigate(false);
            }
        });
    }


    /**
     * Initializes Railways toolbar with actions defined in plugin.xml.
     */
    private void initToolbar() {
        ActionManager am = ActionManager.getInstance();

        // The toolbar is registered in plugin.xml
        ActionGroup actionGroup = (ActionGroup) am.getAction("railways.MainToolbar");

        // Create railways toolbar.
        ActionToolbar toolbar = am.createActionToolbar(ActionPlaces.UNKNOWN, actionGroup, true);

        toolbar.setTargetComponent(actionsPanel);
        actionsPanel.add(toolbar.getComponent(), BorderLayout.CENTER);
    }


    /**
     * Initializes splitter that divides routes table and info panel.
     * We do this manually as there were difficulties with UI designer and
     * the splitter.
     */
    private void initSplitter() {
        // Remove required components from main panel
        routesPanel.remove(routeInfoPanel);
        routesPanel.remove(routeViews);

        mySplitter = new JBSplitter(true, 0.8f);

        mySplitter.setHonorComponentsMinimumSize(true);
        mySplitter.setAndLoadSplitterProportionKey("Railways.SplitterProportion");
        mySplitter.setOpaque(false);
        mySplitter.setShowDividerControls(false);
        mySplitter.setShowDividerIcon(false);

        mySplitter.setFirstComponent(routeViews);
        mySplitter.setSecondComponent(routeInfoPanel);

        routesPanel.add(mySplitter, BorderLayout.CENTER);
    }

    public void setOrientation(boolean isVertical) {
        mySplitter.setOrientation(isVertical);
    }



    /**
     * Called by IntelliJ form builder upon MainForm creation.
     */
    private void createUIComponents() {
        // Create custom table
        routesTable = new RoutesTable(getRouteTableModel(), this);

        // Create tree manually to get is with empty model.
        routesTree = new Tree(new DefaultTreeModel(null));
    }


    public RouteTableModel getRouteTableModel() {
        if (myTableModel == null)
            myTableModel = new RouteTableModel();

        return myTableModel;
    }


    private void showPanel(@NotNull String panelName) {
        if (panelName.equals(currentPanel))
            return;

        infoLink.setVisible(panelName.equals(INFO_PANEL_NAME));
        environmentLbl.setVisible(panelName.equals(INFO_PANEL_NAME));
        setControlsEnabled(panelName.equals(ROUTES_PANEL_NAME));

        ((CardLayout)centerPanel.getLayout()).show(centerPanel, panelName);

        currentPanel = panelName;
    }


    private void updateErrorPanel(int parserError) {
        switch (parserError) {
            case RailsRoutesParser.ERROR_RAKE_TASK_NOT_FOUND:
                RoutesManager.State settings = myDataSource.getRoutesManager().getState();
                infoLbl.setText("Rake task '" + settings.routesTaskName + "' is not found.");
                infoLink.setHyperlinkText("Configure");
                infoLinkAction = LINK_OPEN_SETTINGS;

                AnAction act = ActionManager.getInstance().getAction("Railways.settingsAction");
                infoLink.setIcon(act.getTemplatePresentation().getIcon());
                break;

            default:
                infoLbl.setText("Failed to load routes");
                infoLink.setHyperlinkText("Show details");
                infoLinkAction = LINK_SHOW_STACKTRACE;
                infoLink.setIcon(null);
        }

        infoLink.setVisible(true);
        environmentLbl.setVisible(false);
        routesCounterLbl.setVisible(false);

        updateCounterLabel();
        setControlsEnabled(false);
        environmentLbl.setVisible(false);
    }


    /**
     * Sets the way routes will be represented - using common table or treeview.
     * It can be either ViewConstants.VIEW_MODE_TREE or ViewConstants.VIEW_MODE_TABLE.
     *
     * @param viewMode View mode.
     */
    public void setRoutesViewMode(int viewMode) {
        String panelID = (viewMode == ViewConstants.VIEW_MODE_TREE) ?
                "treePanel" : "tablePanel";

        ((CardLayout) routeViews.getLayout()).show(routeViews, panelID);
    }



    private void navigateToRouteNode(RouteNode node) {
        if ((node == null) || (!node.isLeaf()))
            return;

        node.getRoute().navigate(false);
    }


    private void setControlsEnabled(boolean value) {
        pathFilterField.setEnabled(value);
        routesCounterLbl.setVisible(value);
    }


    /**
     * Updates text of routes counter label. Sets text to undefined when routes
     * list is not visible (info or error panels are shown)
     */
    private void updateCounterLabel() {
        routesCounterLbl.setText(String.format("%d/%d",
                getRouteTableModel().getRowCount(),
                getRouteTableModel().getTotalRoutesCount()));
    }


    public JPanel getRootPanel() {
        return rootPanel;
    }


    /**
     * Shows additional info in information panel.
     *
     * @param route Route which info should be showed or nil if info should be
     *              hidden.
     */
    public void showRouteInfo(@Nullable Route route) {
        currentRoute = route;

        if (route == null) {
            routeLbl.setText(NO_INFO);
            methodLbl.setText(NO_INFO);
            methodLbl.setIcon(null);

            actionLbl.setText(NO_INFO);
            actionLbl.setIcon(null);

            nameLbl.setText(NO_INFO);
        } else {
            routeLbl.setText(route.getPath());
            nameLbl.setText(route.getRouteName());

            methodLbl.setText(route.getRequestMethod().getName());
            methodLbl.setIcon(route.getRequestMethod().getIcon());

            actionLbl.setIcon(route.getActionIcon());
            actionLbl.setToolTipText(null);

            if (route.canNavigate())
                actionLbl.setHyperlinkText(route.getQualifiedActionTitle());
            else
                actionLbl.setText(route.getQualifiedActionTitle());

            if (route instanceof SimpleRoute) {
                RailsActionInfo action = ((SimpleRoute)route).getActionInfo();

                if (action.getPsiMethod() != null)
                    actionLbl.setToolTipText("Go to action declaration");

                else if (action.getPsiClass() != null)
                    actionLbl.setToolTipText("Go to controller declaration");

                else
                    actionLbl.setToolTipText("Cannot find controller declaration");
            }
        }

        routeInfoPanel.revalidate();
        routeInfoPanel.repaint();
    }


    // ----------------------------------------
    //         Railways event handlers

    public void setUpdatedRoutes(RouteList routeList) {
        getRouteTableModel().setRoutes(routeList);

        // Update tree view
        RouteNode root = RouteTreeBuilder.buildTree(routeList);
        routesTree.setModel(new DefaultTreeModel(root));

        showPanel(ROUTES_PANEL_NAME);

        UpdateRoutesListAction.updateIcon(project);
    }


    public void showLoadingMessage() {
        RoutesManager.State settings = myDataSource.getRoutesManager().getState();
        showMessagePanel("Running `rake " + settings.routesTaskName + "`...",
                settings.environment);
    }


    /**
     * Hides panel with routes list and shows panel with information message.
     *
     * @param message Message to show.
     */
    private void showMessagePanel(String message, @Nullable String envName) {
        environmentLbl.setText("Environment: " +
                (envName == null ? "Default" : envName));
        environmentLbl.setVisible(true);

        infoLbl.setText(message);
        showPanel(INFO_PANEL_NAME);
    }


    public void showRoutesUpdateError(int parserError) {
        updateErrorPanel(parserError);
        showPanel(INFO_PANEL_NAME);
    
        UpdateRoutesListAction.updateIcon(project);
    }


    /**
     * Sets RoutesViewPane as a source of all data that should be displayed in
     * the tool window.
     *
     * @param dataSource A pane which data should be displayed in the tool window.
     */
    public void setDataSource(RoutesViewPane dataSource) {
        if (myDataSource == dataSource) return;

        myDataSource = dataSource;

        RouteList routes =
                (dataSource != null) ? dataSource.getRoutesManager().getRouteList() : null;
        getRouteTableModel().setRoutes(routes);
    }


    public void refresh() {
        // Use fireTableRowsUpdated to avoid full tree refresh and to keep selection.
        getRouteTableModel().fireTableRowsUpdated(0,
                getRouteTableModel().getRowCount() - 1);
    }
}