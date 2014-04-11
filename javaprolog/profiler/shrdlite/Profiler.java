package profiler.shrdlite;

import gnu.prolog.vm.PrologException;
import main.Shrdlite;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * Created by Roland on 2014-04-11.
 */
public class Profiler extends JPanel{



    public Profiler(){
        super(new GridLayout(1,0));

        JButton button = new JButton("Run Shrdlite");

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                SwingWorker worker = new SwingWorker() {
                    @Override
                    protected Object doInBackground() throws Exception {
                        String[] args = {"C:\\Abyss Web Server\\htdocs\\AIGroup1\\javaprolog\\testfiles\\testinput.json"};
                        try {
                            Shrdlite.main(args);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        } catch (PrologException e1) {
                            e1.printStackTrace();
                        } catch (CloneNotSupportedException e1) {
                            e1.printStackTrace();
                        }
                        return null;
                    }
                };
                worker.execute();
            }
        });

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Simple", null, button, null);

        tabbedPane.setSelectedIndex(0);

        add(tabbedPane);
    }


    public static void main(String[] args){
        //Create and set up the window.
        JFrame frame = new JFrame("Profiling");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocation(800, 800);

        //Create and set up the content pane.
        Profiler newContentPane = new Profiler();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

}
