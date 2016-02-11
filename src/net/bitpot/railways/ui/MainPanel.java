package net.bitpot.railways.ui;

import com.intellij.ide.CommonActionsManager;
import com.intellij.ide.TreeExpander;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.containers.Convertor;
import com.intellij.util.ui.tree.TreeUtil;
import net.bitpot.railways.actions.UpdateRoutesListAction;
import net.bitpot.railways.ui.table.RoutesTable;
import net.bitpot.railways.ui.tree.RoutesTree;
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
    private RoutesTable routesTable;
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
    private RoutesTree routesTree;
    private MyTreeExpander myTreeExpander = new MyTreeExpander();
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

    private RoutesView view;


    public MainPanel(Project project, RoutesView view) {
        this.project = project;
        this.view = view;

        initToolbar();

        // Init handlers after everything is initialized
        initHandlers();

        getRouteTableModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                updateCounterLabel();
            }
        });

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

        // Add common expand/collapse actions.
        DefaultActionGroup treeGroup = (DefaultActionGroup) am.getAction("railways.TreeActionsGroup");
        AnAction expandAllAction = CommonActionsManager.getInstance()
                .createExpandAllAction(myTreeExpander, getRootPanel());
        treeGroup.add(expandAllAction);

        AnAction collapseAllAction = CommonActionsManager.getInstance()
                .createCollapseAllAction(myTreeExpander, getRootPanel());
        treeGroup.add(collapseAllAction);

        // The toolbar is registered in plugin.xml
        DefaultActionGroup toolbarGroup = (DefaultActionGroup) am.getAction("railways.MainToolbar");

        // Create railways toolbar.
        ActionToolbar toolbar = am.createActionToolbar(ActionPlaces.UNKNOWN, toolbarGroup, true);

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
        routesTree = new RoutesTree(new DefaultTreeModel(null), this);

        new MySpeedSearch(routesTree);

        nameLbl = new LabelWithCopy();
        routeLbl = new LabelWithCopy();
        ((LabelWithCopy)routeLbl).setCopyFormatter(RailwaysUtils.STRIP_REQUEST_FORMAT);
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
        String panelID;
        boolean restoreFocus;

        if (viewMode == ViewConstants.VIEW_MODE_TREE) {
            panelID = "treePanel";
            restoreFocus = routesTable.isFocusOwner();
        } else {
            panelID = "tablePanel";
            restoreFocus = routesTree.isFocusOwner();
        }

        ((CardLayout) routeViews.getLayout()).show(routeViews, panelID);

        if (viewMode == ViewConstants.VIEW_MODE_TREE) {
            routesTree.updateNodeInfo();
            if (restoreFocus)
                routesTree.requestFocusInWindow();
        } else {
            routesTable.updateNodeInfo();
            if (restoreFocus)
                routesTable.requestFocusInWindow();
        }

        pathFilterField.setEnabled(viewMode == ViewConstants.VIEW_MODE_TABLE);
        routesCounterLbl.setEnabled(viewMode == ViewConstants.VIEW_MODE_TABLE);
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

    public RoutesFilter getRouteFilter() {
        return getRouteTableModel().getFilter();
    }


    private final class MyTreeExpander implements TreeExpander {
        @Override
        public boolean canCollapse() {
            return view.getViewMode() == ViewConstants.VIEW_MODE_TREE;
        }

        @Override
        public boolean canExpand() {
            return view.getViewMode() == ViewConstants.VIEW_MODE_TREE;
        }

        @Override
        public void collapseAll() {
            TreeUtil.collapseAll(routesTree, 0);
        }

        @Override
        public void expandAll() {
            TreeUtil.expandAll(routesTree);
        }
    }

    private final static Convertor<TreePath, String> ROUTE_NODE_TO_STRING = new Convertor<TreePath, String>() {
        @Override
        public String convert(TreePath object) {
            RouteNode node = (RouteNode)object.getLastPathComponent();

            return node.isLeaf() ?
                    RailwaysUtils.trimRequestFormat(node.getTitle()) :
                    node.getTitle();
        }
    };

    private class MySpeedSearch extends TreeSpeedSearch {
        public MySpeedSearch(JTree tree) {
            super(tree, ROUTE_NODE_TO_STRING);
        }
    }
}