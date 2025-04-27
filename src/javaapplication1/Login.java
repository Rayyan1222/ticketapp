package javaapplication1;
//Import Statement For Fields//
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
//Class for Login File for JFrame//
@SuppressWarnings("serial")
public class Login extends JFrame {
//Data Access Object Creation//
    Dao conn = new Dao();
//Constructor for Login Screen//
    public Login() {
        super("IIT HELP DESK LOGIN");
        conn = new Dao();
        conn.createTables(); // Creates tables if not present
        setSize(400, 210);
        setLayout(new GridLayout(4, 2));
        setLocationRelativeTo(null);
        //Create UI Components//
        JLabel lblUsername = new JLabel("Username", JLabel.LEFT);
        JLabel lblPassword = new JLabel("Password", JLabel.LEFT);
        JLabel lblStatus = new JLabel(" ", JLabel.CENTER);
        //Create UI Components//
        JTextField txtUname = new JTextField(10);
        JPasswordField txtPassword = new JPasswordField();
        JButton btn = new JButton("Submit");
        JButton btnExit = new JButton("Exit");
        //Login Attempt Unlock//
        lblStatus.setToolTipText("Contact help desk to unlock password");
        lblUsername.setHorizontalAlignment(JLabel.CENTER);
        lblPassword.setHorizontalAlignment(JLabel.CENTER);
        //Components for the UI Window//
        add(lblUsername);  
        add(txtUname);
        add(lblPassword);  
        add(txtPassword);
        add(btn);          
        add(btnExit);
        add(lblStatus);
        //Action Listener to Submit Button//
        btn.addActionListener(new ActionListener() {
        	//Count for Login Attempts//
            int count = 0;
            //Void Method for ActionPerformed//
            @Override
            public void actionPerformed(ActionEvent e) {
            	//Check if user is an admin//
                boolean admin = false;
                //Increment login attempt counter//
                count = count + 1;
                String query = "SELECT * FROM rbasa_users WHERE uname = ? and upass = ?;";
                try (PreparedStatement stmt = conn.getConnection().prepareStatement(query)) {
                	//Username and Password Input//
                    stmt.setString(1, txtUname.getText());
                    stmt.setString(2, txtPassword.getText());
                    //Execute Query//
                    ResultSet rs = stmt.executeQuery();
                    //Check for Matching User//
                    if (rs.next()) {
                        admin = rs.getBoolean("admin"); // Check if the user is an admin
                        new Tickets(admin, txtUname.getText()); // Open the appropriate window (admin or regular user)
                        setVisible(false); //Hide the login window//
                        dispose(); //Close login window//
                      //Credentials are invalid//
                    } else {
                    	lblStatus.setText("Try again! " + (3 - count) + " / 3 attempt(s) left"); // Show attempts left
                        if (count >= 3) { // If maximum attempts are reached
                            JOptionPane.showMessageDialog(null, "Maximum attempts reached. Exiting application.", 
                                "Error", JOptionPane.ERROR_MESSAGE);
                            //Exit Application//
                            System.exit(0);
                        }
                    } 
                }catch (SQLException ex) { //Handle SQL exceptions//
                    ex.printStackTrace();
                }
            }
        });
        //Add Action Listener to Exit Button//
        btnExit.addActionListener(e -> System.exit(0));
        //Make login window visible//
        setVisible(true);
    }
    //Main Method to Launch Login Screen//
    public static void main(String[] args) {
        new Login(); // Launch the login screen
    }
}

