package javaapplication1;
//Create Import Statements//
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
//UI Layout//
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
//Class for Tickets that extends JFrame and implements ActionListener//
@SuppressWarnings("serial")
public class Tickets extends JFrame implements ActionListener {
	//Dao Object//
    Dao dao = new Dao(); 
    Boolean isAdmin = null; // Check if an Admin//
    String loggedInUser = null; // Store logged-in username//

    // Menu objects
    private JMenu mnuFile = new JMenu("File");
    private JMenu mnuAdmin = new JMenu("Admin");
    private JMenu mnuTickets = new JMenu("Tickets");

    // Sub-menu items
    JMenuItem mnuItemExit;
    JMenuItem mnuItemRefresh;  
    JMenuItem mnuItemUpdate;
    JMenuItem mnuItemDelete;
    JMenuItem mnuItemOpenTicket;
    JMenuItem mnuItemViewTicket;
    JMenuItem mnuItemSelectTicket;
    //Constructor Initializes Tickets GUI//
    public Tickets(Boolean isAdminRole, String userName) {
        isAdmin = isAdminRole; // Store Admin Or User//
        loggedInUser = userName; // Store logged-in username//
        createMenu(); //Create Menu//
        prepareGUI(); //Set Up GUI//

        if (isAdmin) {
        	//Show all tickets when admin logs in//
            displayAllTickets(); 
        }
        //If not Admin disable admin options//
        if (!isAdmin) {
            mnuAdmin.setEnabled(false); 
        }

        // Add the WindowListener to handle idle and resizing events
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                revalidate(); //Make sure layout is revalidated//
                repaint();    // Repaint the frame//
            }
        });
    }

    //Create Menu Bar and Items//
    private void createMenu() {
        mnuItemExit = new JMenuItem("Exit");
        mnuItemRefresh = new JMenuItem("Refresh");
        mnuFile.add(mnuItemRefresh);
        mnuFile.add(mnuItemExit);

        mnuItemUpdate = new JMenuItem("Update Ticket");
        mnuItemDelete = new JMenuItem("Delete Ticket");
        mnuAdmin.add(mnuItemUpdate);
        mnuAdmin.add(mnuItemDelete);

        mnuItemOpenTicket = new JMenuItem("Open Ticket");
        mnuItemViewTicket = new JMenuItem("View Tickets");
        mnuItemSelectTicket = new JMenuItem("Select Ticket");
        mnuTickets.add(mnuItemOpenTicket);
        mnuTickets.add(mnuItemViewTicket);
        mnuTickets.add(mnuItemSelectTicket);
        //Add ActionListiner to menu items//
        mnuItemExit.addActionListener(this);
        mnuItemRefresh.addActionListener(this);
        mnuItemUpdate.addActionListener(this);
        mnuItemDelete.addActionListener(this);
        mnuItemOpenTicket.addActionListener(this);
        mnuItemViewTicket.addActionListener(this);
        mnuItemSelectTicket.addActionListener(this);
    }
    //Prepare GUI layout and window settings//
    private void prepareGUI() {
        JMenuBar bar = new JMenuBar();
        bar.add(mnuFile);
        bar.add(mnuAdmin);
        bar.add(mnuTickets);
        setJMenuBar(bar); //Set Menu Bar//

        setLayout(new BorderLayout()); // Use BorderLayout for dynamic content
        getContentPane().setBackground(Color.LIGHT_GRAY);

        addWindowListener(new WindowAdapter() {
        	//Close Application Window Close//
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        setSize(600, 400); //Set Frame Size//
        setLocationRelativeTo(null); //Center the Frame//
        setVisible(true); //Make Frame Visible//
    }

    //Display all tickets for Admin//
    private void displayAllTickets() {
        try {
        	//Get All Ticket Records//
            ResultSet rs = dao.readRecords();
            if (rs != null) {
            	//Create Table and add table to scroll pane//
                JTable table = new JTable(ticketsJTable.buildTableModel(rs));
                JScrollPane scrollPane = new JScrollPane(table);
                add(scrollPane);
                //Make it Visible//
                setVisible(true);
            }
          //SQL Exception//
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    //Handle Menu Item Actions//
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == mnuItemExit) {
        	//Exit Application//
            System.exit(0);
          //Create a new ticket//
        } else if (e.getSource() == mnuItemOpenTicket) {
            String ticketDesc = JOptionPane.showInputDialog(null, "Enter a ticket description:");
            if (ticketDesc == null || ticketDesc.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Ticket creation canceled. Description is required.");
                //Cancel if no Description Provided//
                return;
            }
            //Add Ticket to DB//
            int id = dao.insertRecords(loggedInUser, ticketDesc); // Use loggedInUser as the ticket_issuer
            if (id != 0) {
                JOptionPane.showMessageDialog(null, "Ticket created with ID: " + id);
            } else {
                JOptionPane.showMessageDialog(null, "Failed to create ticket.");
            }
        }
        //View Tickets//
        else if (e.getSource() == mnuItemViewTicket) {
            try {
            	//Table and ResultSet//
                JTable table;
                ResultSet rs;
                if (isAdmin) {
                    rs = dao.readRecords(); // Admins view all tickets//
                } else {
                    rs = dao.readUserTickets(loggedInUser); // Regular users view their tickets//
                }
                if (rs != null) {
                    table = new JTable(ticketsJTable.buildTableModel(rs));
                    JScrollPane scrollPane = new JScrollPane(table);
                    getContentPane().removeAll(); // Clear previous content
                    add(scrollPane); //Add Scroll Pane//
                    revalidate(); //Make table revalidate//
                    repaint();  //Repaint the Table//
                } else {
                	//No Tickets Found if wrong ID Given//
                    JOptionPane.showMessageDialog(null, "No tickets found for the user.", "Info", JOptionPane.INFORMATION_MESSAGE);
                }
              //SQL Exception//
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
//Update Tickets//
else if (e.getSource() == mnuItemUpdate) {
    //Prompt for ticket ID//
    String ticketIdStr = JOptionPane.showInputDialog("Enter ticket ID to update:");
    if (ticketIdStr == null || ticketIdStr.isEmpty()) {
    	//Cancel Ticket Update and give error//
        JOptionPane.showMessageDialog(null, "Ticket update canceled. Ticket ID is required.", 
                "Error", JOptionPane.ERROR_MESSAGE);
        return; // Cancel the update process//
    }

    try {
        int ticketId = Integer.parseInt(ticketIdStr); // Convert ticket ID to integer//

        // Validate if the ticket exists//
        ResultSet rs = dao.selectTicketById(ticketId, loggedInUser, isAdmin);
        //Ticket ID Doesn't exist//
        if (rs == null || !rs.next()) {
            JOptionPane.showMessageDialog(null, "Ticket ID " + ticketId + " doesn't exist.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            return; // Exit if the ticket ID is invalid//
        }

        //Update The Description//
        String newDesc = JOptionPane.showInputDialog("Enter new description:");
        //If no description end the update//
        if (newDesc == null || newDesc.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Ticket update canceled. Description is required.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            return; // Cancel the update process
        }

        //Prompt for new status open or closed//
        String newStatus = JOptionPane.showInputDialog("Enter new status (open, closed):");
        //If nothing is filled cancel the update//
        if (newStatus == null || newStatus.isEmpty() || 
           (!"open".equalsIgnoreCase(newStatus) && !"closed".equalsIgnoreCase(newStatus))) {
            JOptionPane.showMessageDialog(null, "Invalid status. Ticket update canceled.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            return; // Cancel the update process//
        }

        //Update the Ticket//
        dao.updateRecords(ticketId, newDesc, newStatus);
        JOptionPane.showMessageDialog(null, "Ticket #" + ticketId + " updated successfully.", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
      //Error Format for invalid ticket ID//
    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(null, "Invalid ticket ID entered. Please enter a numeric value.", 
                "Error", JOptionPane.ERROR_MESSAGE);
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(null, "An error occurred while validating the ticket ID.", 
                "Error", JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }
}
//Deleting Ticket ID//
else if (e.getSource() == mnuItemDelete) {
    String ticketIdStr = JOptionPane.showInputDialog("Enter the Ticket ID to delete:");
    
    // Check if the input was canceled or left blank//
    if (ticketIdStr == null || ticketIdStr.isEmpty()) {
        JOptionPane.showMessageDialog(null, "Ticket ID is required to delete.", 
                "Error", JOptionPane.ERROR_MESSAGE);
        return; // Exit if no input is provided
    }

    try {
        int ticketId = Integer.parseInt(ticketIdStr); // Convert ticket ID to integer//

        // Validate if the ticket exists//
        ResultSet rs = dao.selectTicketById(ticketId, loggedInUser, isAdmin);
        if (rs == null || !rs.next()) {
            JOptionPane.showMessageDialog(null, "Invalid Ticket ID entered. Ticket does not exist.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            return; // Exit if the ticket ID is invalid//
        }

        // Confirm deletion of ticket//
        int response = JOptionPane.showConfirmDialog(null,
                "Are you sure you want to delete ticket #" + ticketId + "?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        //Deletion Successful message//
        if (response == JOptionPane.YES_OPTION) {
            dao.deleteRecords(ticketId);
            JOptionPane.showMessageDialog(null, "Deletion Successful", 
                    "Delete Status", JOptionPane.INFORMATION_MESSAGE);
        } else {
        	//Backing out ticket deletion//
            JOptionPane.showMessageDialog(null, "Ticket deletion canceled.", 
                    "Info", JOptionPane.INFORMATION_MESSAGE);
        }
      //Invalid Ticket ID entered//
    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(null, "Invalid Ticket ID entered. Please enter a numeric value.", 
                "Error", JOptionPane.ERROR_MESSAGE);
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(null, "An error occurred while validating the ticket ID.", 
                "Error", JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }
}
 //Refresh the table//
 else if (e.getSource() == mnuItemRefresh) {
            refreshTicketTable();
        } else if (e.getSource() == mnuItemSelectTicket) {
            String ticketIdStr = JOptionPane.showInputDialog("Enter the Ticket ID to view:");
            if (ticketIdStr != null && !ticketIdStr.isEmpty()) {
                try {
                    int ticketId = Integer.parseInt(ticketIdStr); //Parse Ticket ID//
                    //Get Ticket Details//
                    ResultSet rs = dao.selectTicketById(ticketId, loggedInUser, isAdmin);
                    if (rs != null && rs.next()) {
                        String ticketDetails = "Ticket ID: " + rs.getInt("ticket_id") +
                                "\nIssuer: " + rs.getString("ticket_issuer") +
                                "\nDescription: " + rs.getString("ticket_description") +
                                "\nOpen Time: " + rs.getTimestamp("open_time") +
                                "\nStatus: " + rs.getString("status");
                        JOptionPane.showMessageDialog(null, ticketDetails, "Ticket Details", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                    	//Wrong access entered//
                        JOptionPane.showMessageDialog(null, "You do not have access to this ticket or it does not exist.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                	//Wrong ID entered//
                    JOptionPane.showMessageDialog(null, "Invalid Ticket ID entered.", "Error", JOptionPane.ERROR_MESSAGE);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } else {
            	//Ticket ID can not be blank//
                JOptionPane.showMessageDialog(null, "Ticket ID cannot be blank.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refreshTicketTable() {
        try {
            ResultSet rs;
            if (isAdmin) {
                // Admins can see all tickets//
                rs = dao.readRecords();
            } else {
                // Regular users can only see their own tickets//
                rs = dao.readUserTickets(loggedInUser);
            }

            if (rs != null) {
            	//Build table and wrap scroll pane//
                JTable table = new JTable(ticketsJTable.buildTableModel(rs));
                JScrollPane scrollPane = new JScrollPane(table);
                getContentPane().removeAll(); // Clear old data//
                add(scrollPane, BorderLayout.CENTER); //add new data//
                revalidate(); //Revalidate Layout//
                repaint(); //Repaint layout///
            } else {
                JOptionPane.showMessageDialog(null, "No tickets found.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
         //SQL Exception//
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}






