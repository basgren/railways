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
import com.intellij.util.ui.UIUtil;
import net.bitpot.railways.actions.UpdateRoutesListAction;
import net.bitpot.railways.api.Railways;
import net.bitpot.railways.api.RoutesManagerListener;
import net.bitpot.railways.models.Route;
import net.bitpot.railways.models.RouteTableModel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 
 */
public class MainPanel implements RoutesManagerListener
{
    @SuppressWarnings("unused")
    private static Logger log = Logger.getInstance(MainPanel.class.getName());

    // Names of cards for main panel that contains several pages.
    // Names should be the same as specified in GUI designer for appropriate panels.
    private final static String ROUTES_CARD_NAME = "routesCard"; // Main page with routes table.
    private final static String INFO_CARD_NAME = "infoCard"; // Panel with message/error information.

    private final static String NO_INFO = "-";

    private RouteTableModel model;
    
    private JPanel rootPanel;
    private JTable routesTable;
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


    private CardLayout cardLayout;
    private boolean routesHidden = false;


    private Project project;
    private Railways api;


    // Contains route which information is shown in the info panel. Contains null if no route is selected.
    private Route currentRoute;


    public MainPanel(Project project)
    {
        this.project = project;

        api = Railways.getAPI(project);

        initToolbar();

        // Init handlers after everything is initialized
        initHandlers();

        model = new RouteTableModel();

        // TODO: should we set routes here?
        //model.setRoutes(api.getRoutes());
        routesTable.setModel(model);

        model.addTableModelListener(new TableModelListener()
        {
            @Override
            public void tableChanged(TableModelEvent e)
            {
                updateCounterLabel();
            }
        });

        routesTable.setDefaultRenderer(Object.class, new RouteTableCellRenderer(model.getFilter()));
        routesTable.setRowHeight(20);

        cardLayout = (CardLayout)(cardsPanel.getLayout());

        showErrorLink.setHyperlinkText("Show details");

        updateCounterLabel();

        // Update route info panel
        showRouteInfo(null);

        //PopupHandler.installPopupHandler(routesTable, "railways.PopupMenu", ActionPlaces.UNKNOWN);


        //api.initRouteList();
    }



    /**
     * Hides panel with routes list and shows panel with information message.
     *
     * @param message Message to show.
     */
    private void showMessagePanel(String message)
    {
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
    private void showErrorPanel()
    {
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
    private void showRoutesPanel()
    {
        routesHidden = false;
        updateCounterLabel();
        setControlsEnabled(true);

        routesCounterLbl.setVisible(true);
        cardLayout.show(cardsPanel, ROUTES_CARD_NAME);
    }



    private void createUIComponents()
    {
        routesTable = new RoutesTable();
    }



    private void initHandlers()
    {
        // TODO: restore listeners.
        //api.getRoutesManager().addListener(this);


        // When filter field text is changed, routes table will be refiltered.
        pathFilterField.getDocument().addDocumentListener(new DocumentAdapter()
        {
            @Override
            protected void textChanged(DocumentEvent e)
            {
                model.getFilter().setPathFilter(pathFilterField.getText());
            }
        });


        // Register mouse handler to handle double-clicks.
        // Double clicking a row will navigate to the action of selected route.
        routesTable.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2) {

                    JTable target = (JTable)e.getSource();
                    int viewRow = target.rowAtPoint(e.getPoint());
                    if (viewRow < 0)
                        return;

                    int row = target.convertRowIndexToModel(viewRow);
                    Route route = model.getRoute(row);
                    api.navigateToRouteAction(route);
                }
            }
        });

        // Bind handler that
        routesTable.getSelectionModel().addListSelectionListener(new RouteSelectionListener(routesTable));

        showErrorLink.addHyperlinkListener(new HyperlinkListener()
        {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e)
            {
                Railways.getAPI(project).showErrorInfo();
            }
        });


        actionLbl.addHyperlinkListener(new HyperlinkListener()
        {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e)
            {
                if (currentRoute != null)
                    api.navigateToRouteAction(currentRoute);
            }
        });
    }




    private void setControlsEnabled(boolean value)
    {
        pathFilterField.setEnabled(value);
        routesCounterLbl.setEnabled(value);
    }


    /**
     * Updates text of routes counter label. Sets text to undefined when routes list is not visible
     * (info or error panels are shown)
     */
    private void updateCounterLabel()
    {
        if (routesHidden)
            routesCounterLbl.setText("--/--");
        else
            routesCounterLbl.setText(String.format("%d/%d", model.getRowCount(), model.getTotalRoutesCount()));
    }


    private void initToolbar()
    {
        ActionManager am = ActionManager.getInstance();

        // The toolbar is registered in plugin.xml
        ActionGroup actionGroup = (ActionGroup)am.getAction("railways.MainToolbar");

        // Create railways toolbar.
        ActionToolbar toolbar = am.createActionToolbar(ActionPlaces.UNKNOWN, actionGroup, true);

        //toolbar.setTargetComponent(rootPanel);
        //rootPanel.add(toolbar.getComponent(), BorderLayout.NORTH);

        toolbar.setTargetComponent(actionsPanel);
        actionsPanel.add(toolbar.getComponent(), BorderLayout.CENTER);
    }

    public JPanel getRootPanel()
    {
        return rootPanel;
    }


    /**
     * Shows additional info in information panel.
     * @param route Route which info should be showed or nil if info should be hidden.
     */
    private void showRouteInfo(@Nullable Route route)
    {
        currentRoute = route;

        if (route == null)
        {
            routeLbl.setText(NO_INFO);
            methodLbl.setText(NO_INFO);
            methodLbl.setIcon(null);

            actionLbl.setHyperlinkText("Test");
            actionLbl.setText(NO_INFO);

            nameLbl.setText(NO_INFO);
        }
        else
        {
            routeLbl.setText(route.getPath());
            methodLbl.setText(route.getRequestType().getName());
            methodLbl.setIcon(route.getIcon());

            switch (route.getType())
            {
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

    @Override
    public void routesUpdated()
    {
        // Railways can invoke this event from another thread
        UIUtil.invokeLaterIfNeeded(new Runnable() {
            public void run() {
                model.setRoutes(api.getRoutes());
                showRoutesPanel();
                UpdateRoutesListAction.updateIcon(project);
            }
        });
    }

    @Override
    public void beforeRoutesUpdate()
    {
        // Railways can invoke this event from another thread
        UIUtil.invokeLaterIfNeeded(new Runnable() {
            public void run() {
                showMessagePanel("Loading routes...");
            }
        });
    }

    @Override
    public void routesUpdateError()
    {
        UIUtil.invokeLaterIfNeeded(new Runnable()
        {
            @Override
            public void run()
            {
                showErrorPanel();
                UpdateRoutesListAction.updateIcon(project);
            }
        });
    }




    private class RouteSelectionListener implements ListSelectionListener
    {
        private JTable table;


        public RouteSelectionListener(JTable table)
        {
            this.table = table;
        }

        @Override
        public void valueChanged(ListSelectionEvent e)
        {
            if (e.getValueIsAdjusting())
                return;

            RouteTableModel model = (RouteTableModel)table.getModel();

            int id = table.convertRowIndexToModel(table.getSelectedRow());
            Route route = null;

            if (id >= 0)
                route = model.getRoute(id);

            //log.debug(String.format("Selected first index: %d, model index: %d, route: %s, %s, %s, %s", e.getFirstIndex(), table.convertRowIndexToModel(e.getFirstIndex()),
            //        route.path, route.controller + "#" + route.action, route.httpMethod, route.name));
            showRouteInfo(route);
        }
    }
}
