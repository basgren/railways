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
import net.bitpot.railways.actions.UpdateRoutesListAction;
import net.bitpot.railways.models.*;
import net.bitpot.railways.models.routes.SimpleRoute;
import net.bitpot.railways.parser.RailsRoutesParser;
import net.bitpot.railways.routesView.RoutesManager;
import net.bitpot.railways.routesView.RoutesView;
import net.bitpot.railways.routesView.RoutesViewPane;
import net.bitpot.railways.utils.RailwaysUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.*;
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
    private final static String ROUTES_CARD_NAME = "routesCard"; // Main page with routes table.
    private final static String INFO_CARD_NAME = "infoCard"; // Panel with message/error information.

    private final static String NO_INFO = "-";

    private static final int LINK_OPEN_SETTINGS = 1;
    private static final int LINK_SHOW_STACKTRACE = 2;


    private RouteTableModel myTableModel;

    private JPanel rootPanel;
    private JBTable routesTable;
    private JTextField pathFilterField;
    private JPanel cardsPanel;
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
    private JPanel mainRoutePanel;
    private JBLabel environmentLbl;


    private CardLayout cardLayout;
    private boolean routesHidden = false;


    private Project project;

    // A pane that is used as a data source for the Routes panel.
    private RoutesViewPane myDataSource = null;

    // Contains route which information is shown in the info panel.
    // Contains null if no route is selected.
    private Route currentRoute;

    private int infoLinkAction;

    private JBSplitter mySplitter;


    public MainPanel(Project project) {
        this.project = project;

        initToolbar();

        // Init handlers after everything is initialized
        initHandlers();

        myTableModel = new RouteTableModel();
        routesTable.setModel(myTableModel);

        myTableModel.addTableModelListener(e -> updateCounterLabel());

        routesTable.setDefaultRenderer(Route.class,
                new RouteCellRenderer(myTableModel.getFilter()));

        routesTable.setDefaultRenderer(Object.class,
                new FilterHighlightRenderer(myTableModel.getFilter()));

        routesTable.setRowHeight(20);

        cardLayout = (CardLayout) (cardsPanel.getLayout());

        updateCounterLabel();

        // Update route info panel
        showRouteInfo(null);

        initSplitter();
    }


    private void createUIComponents() {
        routesTable = new RoutesTable();
        nameLbl = new LabelWithCopy();

        routeLbl = new LabelWithCopy();
        ((LabelWithCopy)routeLbl).setCopyFormatter(RailwaysUtils.STRIP_REQUEST_FORMAT);
    }


    /**
     * Initializes splitter that divides routes table and info panel.
     * We do this manually as there were difficulties with UI designer and
     * the splitter.
     */
    private void initSplitter() {
        // Remove required components from main panel
        mainRoutePanel.remove(routeInfoPanel);
        mainRoutePanel.remove(routesScrollPane);

        mySplitter = new JBSplitter(true, 0.8f);

        mySplitter.setHonorComponentsMinimumSize(true);
        mySplitter.setAndLoadSplitterProportionKey("Railways.SplitterProportion");
        mySplitter.setOpaque(false);
        mySplitter.setShowDividerControls(false);
        mySplitter.setShowDividerIcon(false);

        mySplitter.setFirstComponent(routesScrollPane);
        mySplitter.setSecondComponent(routeInfoPanel);

        mainRoutePanel.add(mySplitter, BorderLayout.CENTER);
    }


    public void setOrientation(boolean isVertical) {
        mySplitter.setOrientation(isVertical);
    }


    /**
     * Hides panel with route list and shows panel with information message.
     *
     * @param message Message to show.
     */
    private void showMessagePanel(String message, @Nullable String envName) {
        infoLbl.setText(message);
        environmentLbl.setText("Environment: " + (envName == null ? "Default" : envName));
        environmentLbl.setVisible(true);
        infoLink.setVisible(false);
        routesHidden = true;
        updateCounterLabel();
        setControlsEnabled(false);

        cardLayout.show(cardsPanel, INFO_CARD_NAME);
    }


    /**
     * Hides routes panel and shows panel with error message and with link that shows dialog with error details
     */
    private void showErrorPanel(int parserError) {
        if (parserError == RailsRoutesParser.ERROR_RAKE_TASK_NOT_FOUND) {
            RoutesManager.State settings = myDataSource.getRoutesManager().getState();
            infoLbl.setText("Rake task '" + settings.routesTaskName + "' is not found.");
            infoLink.setHyperlinkText("Configure");
            infoLinkAction = LINK_OPEN_SETTINGS;

            AnAction act = ActionManager.getInstance().getAction("railways.settingsAction");
            infoLink.setIcon(act.getTemplatePresentation().getIcon());
        } else {
            infoLbl.setText("Failed to load routes");
            infoLink.setHyperlinkText("Show details");
            infoLinkAction = LINK_SHOW_STACKTRACE;
            infoLink.setIcon(null);
        }


        infoLink.setVisible(true);
        environmentLbl.setVisible(false);
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


    private void initHandlers() {
        // When filter field text is changed, routes table will be refiltered.
        pathFilterField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                myTableModel.getFilter().setFilterText(pathFilterField.getText());
            }
        });

        // Register mouse handler to handle double-clicks.
        // Double clicking a row will navigate to the action of selected route.
        routesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    JTable target = (JTable) e.getSource();
                    navigateToViewRow(target.rowAtPoint(e.getPoint()));
                }
            }
        });


        routesTable.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    JTable target = (JTable) e.getSource();
                    navigateToViewRow(target.getSelectedRow());
                }
            }
        });

        // Bind handler that
        routesTable.getSelectionModel().addListSelectionListener(
                new RouteSelectionListener(routesTable));

        infoLink.addHyperlinkListener(e -> {
            RoutesManager rm = RoutesView.getInstance(project).getCurrentRoutesManager();
            if (rm == null)
                return;

            if (infoLinkAction == LINK_OPEN_SETTINGS) {
                RailwaysUtils.invokeAction("railways.settingsAction", project);
            } else {
                RailwaysUtils.showErrorInfo(rm);
            }
        });

        actionLbl.addHyperlinkListener(e -> {
            if (currentRoute != null)
                currentRoute.navigate(false);
        });
    }


    /**
     * Navigates to a route in specified viewRow, if row exists.
     * @param viewRow Row index which contains route to navigate to.
     */
    private void navigateToViewRow(int viewRow) {
        if (viewRow < 0)
            return;

        int row = routesTable.convertRowIndexToModel(viewRow);
        myTableModel.getRoute(row).navigate(false);
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


    private void initToolbar() {
        ActionManager am = ActionManager.getInstance();

        // The toolbar is registered in plugin.xml
        ActionGroup actionGroup = (ActionGroup) am.getAction("railways.MainToolbar");

        // Create railways toolbar.
        ActionToolbar toolbar = am.createActionToolbar(ActionPlaces.UNKNOWN, actionGroup, true);

        toolbar.setTargetComponent(actionsPanel);
        actionsPanel.add(toolbar.getComponent(), BorderLayout.CENTER);
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
        myTableModel.setRoutes(routeList);
        showRoutesPanel();
        UpdateRoutesListAction.updateIcon(project);
    }


    public void showLoadingMessage() {
        RoutesManager.State settings = myDataSource.getRoutesManager().getState();
        showMessagePanel("Running `rake " + settings.routesTaskName + "`...",
                settings.environment);
    }


    public void showRoutesUpdateError(int parserError) {
        showErrorPanel(parserError);
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


    public void refresh() {
        // Use fireTableRowsUpdated to avoid full tree refresh and to keep selection.
        myTableModel.fireTableRowsUpdated(0, myTableModel.getRowCount() - 1);
    }

    public RoutesFilter getRouteFilter() {
        return myTableModel.getFilter();
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