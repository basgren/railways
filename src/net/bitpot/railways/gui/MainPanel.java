package net.bitpot.railways.gui;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.ui.treeStructure.Tree;
import net.bitpot.railways.actions.UpdateRoutesListAction;
import net.bitpot.railways.models.*;
import net.bitpot.railways.parser.RouteTreeBuilder;
import net.bitpot.railways.routesView.RoutesManager;
import net.bitpot.railways.routesView.RoutesView;
import net.bitpot.railways.routesView.RoutesViewPane;
import net.bitpot.railways.utils.RailwaysUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
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
    private final static String ROUTES_CARD_NAME = "routesCard"; // Main page with routes table.
    private final static String INFO_CARD_NAME = "infoCard"; // Panel with message/error information.
    private final static String ROUTES_TREE_CARD_NAME = "routesTreeCard"; // Panel with routes tree

    private final static String NO_INFO = "-";

    private RouteTableModel myTableModel;

    private JPanel rootPanel;
    private JBTable routesTable;
    private JTextField pathFilterField;
    private JPanel cardsPanel;
    private HyperlinkLabel showErrorLink;
    private JLabel infoLbl;
    private JLabel routesCounterLbl;


    private JPanel routeInfoPnl;
    private HyperlinkLabel actionLbl;
    private JLabel nameLbl;
    private JLabel methodLbl;
    private JLabel routeLbl;
    private JPanel topPanel;
    private JPanel actionsPanel;
    private JBScrollPane routesScrollPane;
    private JPanel routesListPanel;
    private JPanel routesErrorPanel;
    private JPanel routesTreeviewPanel;
    private JTree routesTree;


    private CardLayout cardLayout;
    private boolean routesHidden = false;


    private Project project;

    // A pane that is used as a data source for the Routes panel.
    private RoutesViewPane myDataSource = null;

    // Contains route which information is shown in the info panel.
    // Contains null if no route is selected.
    private Route currentRoute;


    public MainPanel(Project project) {
        this.project = project;

        initToolbar();

        // Init handlers after everything is initialized
        initHandlers();

        myTableModel = new RouteTableModel();
        routesTable.setModel(myTableModel);

        myTableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                updateCounterLabel();
            }
        });

        // Init routes table
        routesTable.setDefaultRenderer(Route.class,
                new RouteCellRenderer(myTableModel.getFilter()));

        routesTable.setDefaultRenderer(Object.class,
                new RouteTableCellRenderer(myTableModel.getFilter()));

        routesTable.setRowHeight(20);

        // Init routes tree
        routesTree.setCellRenderer(new RouteTreeCellRenderer());


        cardLayout = (CardLayout) (cardsPanel.getLayout());

        showErrorLink.setHyperlinkText("Show details");

        updateCounterLabel();

        // Update route info panel
        showRouteInfo(null);
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
     * Hides panel with routes list and shows panel with information message.
     *
     * @param message Message to show.
     */
    private void showMessagePanel(String message) {
        infoLbl.setText(message);
        showErrorLink.setVisible(false);
        routesHidden = true;
        updateCounterLabel();
        setControlsEnabled(false);

        cardLayout.show(cardsPanel, INFO_CARD_NAME);
    }


    /**
     * Hides routes panel and shows panel with error message and with link that shows dialog with error details
     */
    private void showErrorPanel() {
        infoLbl.setText("Failed to load routes");
        showErrorLink.setVisible(true);
        routesCounterLbl.setVisible(false);
        routesHidden = true;
        updateCounterLabel();
        setControlsEnabled(false);

        cardLayout.show(cardsPanel, INFO_CARD_NAME);
    }


    /**
     * Show panel that contains routes table, hiding any other panel (information or error).
     */
    private void showRoutesPanel() {
        routesHidden = false;
        updateCounterLabel();
        setControlsEnabled(true);

        routesCounterLbl.setVisible(true);
        cardLayout.show(cardsPanel, ROUTES_CARD_NAME);
    }


    private void showRoutesTreePanel() {
        routesHidden = false;
        updateCounterLabel();
        setControlsEnabled(true);

        routesCounterLbl.setVisible(true);
        cardLayout.show(cardsPanel, ROUTES_TREE_CARD_NAME);
    }


    private void createUIComponents() {
        // Create custom table
        routesTable = new RoutesTable();

        // Create tree manually to get is with empty model.
        routesTree = new Tree(new DefaultTreeModel(null));
    }


    private void initHandlers() {
        // When filter field text is changed, routes table will be refiltered.
        pathFilterField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent e) {
                myTableModel.getFilter().setPathFilter(pathFilterField.getText());
            }
        });

        // Register mouse handler to handle double-clicks.
        // Double clicking a row will navigate to the action of selected route.
        routesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {

                    JTable target = (JTable) e.getSource();
                    int viewRow = target.rowAtPoint(e.getPoint());
                    if (viewRow < 0)
                        return;

                    int row = target.convertRowIndexToModel(viewRow);
                    Route route = myTableModel.getRoute(row);
                    route.navigate(false);
                }
            }
        });

        // Bind handler that
        routesTable.getSelectionModel().addListSelectionListener(
                new RouteSelectionListener(routesTable));

        showErrorLink.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                RoutesManager rm = RoutesView.getInstance(project).getCurrentRoutesManager();
                if (rm != null)
                    RailwaysUtils.showErrorInfo(rm);
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


    private void setControlsEnabled(boolean value) {
        pathFilterField.setEnabled(value);
        routesCounterLbl.setEnabled(value);
    }


    /**
     * Updates text of routes counter label. Sets text to undefined when routes
     * list is not visible (info or error panels are shown)
     */
    private void updateCounterLabel() {
        if (routesHidden)
            routesCounterLbl.setText("--/--");
        else
            routesCounterLbl.setText(String.format("%d/%d",
                    myTableModel.getRowCount(), myTableModel.getTotalRoutesCount()));
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
    private void showRouteInfo(@Nullable Route route) {
        currentRoute = route;

        if (route == null) {
            routeLbl.setText(NO_INFO);
            methodLbl.setText(NO_INFO);
            methodLbl.setIcon(null);

            actionLbl.setHyperlinkText("Test");
            actionLbl.setText(NO_INFO);

            nameLbl.setText(NO_INFO);
        } else {
            routeLbl.setText(route.getPath());
            methodLbl.setText(route.getRequestMethod().getName());
            methodLbl.setIcon(route.getIcon());

            switch (route.getType()) {
                case Route.MOUNTED:
                    actionLbl.setText(String.format("%s (mounted)", route.getControllerMethodName()));
                    break;

                case Route.REDIRECT:
                    actionLbl.setText("[redirect]");
                    break;

                default:
                    actionLbl.setHyperlinkText(route.getControllerMethodName());
            }


            nameLbl.setText(route.getRouteName());
        }

        routeInfoPnl.revalidate();
        routeInfoPnl.repaint();
    }


    // ----------------------------------------
    //         Railways event handlers



    public void setUpdatedRoutes(RouteList routeList) {
        myTableModel.setRoutes(routeList);

        // TODO: restore after debug
        showRoutesPanel();

        updateRoutesTree(routeList);
        showRoutesTreePanel();

        UpdateRoutesListAction.updateIcon(project);
    }


    private void updateRoutesTree(RouteList routeList) {
        RouteNode root = RouteTreeBuilder.buildTree(routeList);

        routesTree.setModel(new DefaultTreeModel(root));
    }


    public void showLoading() {
        showMessagePanel("Loading routes...");
    }


    public void showRoutesUpdateError() {
        showErrorPanel();
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
        myTableModel.setRoutes(routes);
    }


    private class RouteSelectionListener implements ListSelectionListener {
        private JTable table;


        public RouteSelectionListener(JTable table) {
            this.table = table;
        }


        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting())
                return;

            RouteTableModel model = (RouteTableModel) table.getModel();

            int id = table.convertRowIndexToModel(table.getSelectedRow());
            Route route = null;

            if (id >= 0)
                route = model.getRoute(id);

            showRouteInfo(route);
        }
    }
}