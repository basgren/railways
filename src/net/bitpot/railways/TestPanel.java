package net.bitpot.railways;

import com.intellij.idea.LoggerFactory;
import com.intellij.mock.MockProject;
import com.intellij.openapi.command.impl.DummyProject;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import net.bitpot.railways.gui.MainPanel;
import net.bitpot.railways.parser.RailsRoutesParser;

import javax.swing.*;
import java.io.FileNotFoundException;

/**
 * Launcher for a form that contains Railways panel. Used for test purposes to avoid launching of sandbox IDE.
 */
public class TestPanel
{
    /**
     * Testing dialog
     * @param args launch arguments
     */
    public static void main(String[] args)
    {
        //Logger.setFactory(LoggerFactory.getInstance());

        @SuppressWarnings("unused")
        Logger log = Logger.getInstance(TestPanel.class.getName());

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                TestPanel testPanel = new TestPanel();
                testPanel.run();
            }
        });
    }



    public void run()
    {
        JFrame frame = new JFrame("Railways test frame");


        MainPanel mainPanel = new MainPanel(null);

        frame.add(mainPanel.getRootPanel());
        initFrame(frame);

        initRouteList();

        frame.setVisible(true);
    }


    private void initFrame(JFrame f)
    {
        f.pack();
        f.setSize(300, 200);
        f.setLocationRelativeTo(null);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }


    private void initRouteList()
    {
        RailsRoutesParser parser = new RailsRoutesParser();
        try
        {

            parser.parseFile("test/data/parserTest_1.txt");

        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

    }
}
