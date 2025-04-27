package javaapplication1;
//Create Import Statements//
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//Create Class for Data Access Object//
public class Dao {
	//Static Connection for Database and Queries//
    static Connection connect = null;
    Statement statement = null;

    // Constructor
    public Dao() {}

    // Establish database connection
    public Connection getConnection() {
        try {
        	//Establish connection to the database//
            connect = DriverManager.getConnection("jdbc:mysql://www.papademas.net:3307/tickets?autoReconnect=true&useSSL=false&user=fp411&password=411");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connect;
    }

    // Create tables if they do not exist
    public void createTables() {
    	//SQL Queries for Tickets and Tables//
        final String createTicketsTable = "CREATE TABLE rbasa5_tickets(ticket_id INT AUTO_INCREMENT PRIMARY KEY, ticket_issuer VARCHAR(30), ticket_description VARCHAR(200), open_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, end_time TIMESTAMP NULL, status VARCHAR(10) DEFAULT 'open')";
        final String createUsersTable = "CREATE TABLE rbasa_users(uid INT AUTO_INCREMENT PRIMARY KEY, uname VARCHAR(30), upass VARCHAR(30), admin int)";
        
        try {
        	//Create Statement Object//
            statement = getConnection().createStatement();
            //Execute Query for Tickets and Users Tables//
            statement.executeUpdate(createTicketsTable);
            statement.executeUpdate(createUsersTable);
            //Print Statement for Created Tables//
            System.out.println("Created tables in the given database...");
            //Close Statement and Connection//
            statement.close();
            connect.close();
        } catch (SQLException e) {
        	//Print error message if table creation fails//
            System.out.println(e.getMessage());
        }
        addUsers();  // Add default users from CSV
    }

    // Add users to the database from userlist.csv
    public void addUsers() {
        String sql; //Query String
        Statement statement;
        BufferedReader br;
        //List to store data from CSV File//
        List<List<String>> array = new ArrayList<>();
        
        try {
        	//Read user data from CSV File//
            br = new BufferedReader(new FileReader(new File("./userlist.csv")));
            String line;
            while ((line = br.readLine()) != null) {
            	//Store each line into Array
                array.add(Arrays.asList(line.split(",")));
            }
        } catch (Exception e) {
        	//Print Statement for Error//
            System.out.println("There was a problem loading the file");
        }

        try {
        	//Create statement Object//
            statement = getConnection().createStatement();
            for (List<String> rowData : array) {
            	//Get uname from CSV row//
                String uname = rowData.get(0);
                // Check if the user already exists
                String checkUserQuery = "SELECT COUNT(*) FROM rbasa_users WHERE uname = ?";
                try (PreparedStatement checkStmt = getConnection().prepareStatement(checkUserQuery)) {
                	//Set uname parameter//
                    checkStmt.setString(1, uname);
                    //Execute the Query//
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next() && rs.getInt(1) == 0) {
                        // User doesn't exist, so insert the new user
                        sql = "INSERT INTO rbasa_users(uname, upass, admin) VALUES('" + rowData.get(0) + "', '" + rowData.get(1) + "', '" + rowData.get(2) + "');";
                        statement.executeUpdate(sql);
                        //Log success//
                        System.out.println("User " + uname + " added to the database.");
                    } else {
                    	//Log if user exists//
                        System.out.println("User " + uname + " already exists.");
                    }
                }
            }
            //Close Statement//
            statement.close();
        } catch (SQLException e) {
        	//Print Stack trace if user adding fails//
            e.printStackTrace();
        }
    }

    // Insert a new ticket into the database
    public int insertRecords(String ticketIssuer, String ticketDesc) {
    	//Variable to store generated ticket ID//
        int id = 0;
        try {
        	//SQL Query to insert a new ticket//
            PreparedStatement stmt = getConnection().prepareStatement(
                "INSERT INTO rbasa5_tickets(ticket_issuer, ticket_description) VALUES(?, ?)",
                //Return auto-generated keys//
                Statement.RETURN_GENERATED_KEYS
            );
            //Set ticket Issuer and ticket Description//
            stmt.setString(1, ticketIssuer); 
            stmt.setString(2, ticketDesc);
            stmt.executeUpdate(); //Execute insert query//
            //Get auto--generated keys//
            ResultSet resultSet = stmt.getGeneratedKeys();
            if (resultSet.next()) {
                id = resultSet.getInt(1); // Retrieve generated ticket ID//
            }
        } catch (SQLException e) {
        	//Print stack trace if insertion fails//
            e.printStackTrace();
        }
        //Return generated ticket ID//
        return id;
    }


    // Retrieve all records (tickets) from the database
    public ResultSet readRecords() {
    	//Variable to store query results//
        ResultSet results = null;
        try {
        	//Create a statement object and Execute Query//
            statement = getConnection().createStatement();
            results = statement.executeQuery("SELECT ticket_id, ticket_issuer, ticket_description, open_time, end_time, status FROM rbasa5_tickets");
            //Check for SQL Exception//
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
        //Return ResultSet//
        return results;
    }


    // Method to retrieve tickets for a specific user (for regular users)
    public ResultSet readUserTickets(String userName) {
    	//Variable to store query results//
        ResultSet results = null;
        try {
        	//SQL Query to retrieve tickets for a specific user//
        	PreparedStatement stmt = getConnection().prepareStatement("SELECT ticket_id, ticket_issuer, ticket_description, open_time, end_time, status FROM rbasa5_tickets WHERE ticket_issuer = ?");
            stmt.setString(1, userName); // Only fetch tickets created by the logged-in user
            results = stmt.executeQuery(); //Execute the query//
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //Return ResultSet
        return results;
    }


 // Update ticket description and status in the database
    public void updateRecords(int ticketId, String newDescription, String newStatus) {
        String updateQuery = "UPDATE rbasa5_tickets SET ticket_description = ?, status = ?, end_time = ? WHERE ticket_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(updateQuery)) {
            stmt.setString(1, newDescription);  // Set new description
            stmt.setString(2, newStatus);       // Set new status

            // Set end_time based on status//
            if ("closed".equalsIgnoreCase(newStatus)) {
                stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis())); // Set end_time to current time
            } else {
                stmt.setNull(3, Types.TIMESTAMP); // Set end_time to NULL for open tickets
            }
            //Set Ticket_ID//
            stmt.setInt(4, ticketId);
            //Execute Update Query//
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Ticket updated successfully.");//Log Success//
            } else {
                System.out.println("Ticket ID not found."); //Log if ticket ID is not Found//
            }
          //SQL Exception//
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //Delete a ticket from the database//
    public void deleteRecords(int ticketId) {
        String deleteQuery = "DELETE FROM rbasa5_tickets WHERE ticket_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(deleteQuery)) {
            stmt.setInt(1, ticketId);  // Set the ticket ID
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Ticket #" + ticketId + " deleted."); //Log Success//
            } else {
                System.out.println("Ticket ID not found."); //Log if ticket ID is not found//
            }
            //SQL Exception//
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    // Retrieve a ticket by its ID
    public ResultSet selectTicketById(int ticketId, String loggedInUser, boolean isAdmin) {
        ResultSet result = null; //Store Query Results
        String query;

        if (isAdmin) {
            // Admin can fetch any ticket by ID
            query = "SELECT ticket_id, ticket_issuer, ticket_description, open_time, end_time, status FROM rbasa5_tickets WHERE ticket_id = ?";
        } else {
            // Regular user can only fetch their own tickets
            query = "SELECT ticket_id, ticket_issuer, ticket_description, open_time, end_time, status FROM rbasa5_tickets WHERE ticket_id = ? AND ticket_issuer = ?";
        }

        try {
        	//Create a prepared statement//
            PreparedStatement pstmt = getConnection().prepareStatement(query);
            //set ticket id parameter//
            pstmt.setInt(1, ticketId);
            if (!isAdmin) {
                pstmt.setString(2, loggedInUser); // Add user filter for if not an admin//
            }
            result = pstmt.executeQuery(); //Execute the Query
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //Return ResultSet//
        return result;
    }
}




