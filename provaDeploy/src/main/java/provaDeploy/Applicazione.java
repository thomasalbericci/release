package provaDeploy;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/Assignment1")
public class Applicazione extends HttpServlet {

	public static void main(String[] args) {
		Applicazione a = new Applicazione();
		a.doPost(null, null);
		
	}

	private static final long serialVersionUID = 1L;
	String userID, password;

	public Applicazione() {
		super();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		String userID;
		String password;
		if (request != null) {
			userID = request.getParameter("userID");
			password = request.getParameter("password");
		}else {
			Scanner keyboard = new Scanner(System.in);
			System.out.println("Inserisci username: ");
			userID = keyboard.nextLine();
			System.out.println("Inserisci password: ");
			password = keyboard.nextLine();
			keyboard.close();
		}
		Connection con = null;
		try {
			con = createTable();
			controllaUtente(userID, password, con, response);
		} catch (Exception e1) {
			e1.printStackTrace();
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void controllaUtente(String userID, String pass, Connection con, HttpServletResponse response) {

		String query = "Select * from tabella";
		Statement stmt = null;
		boolean vero = true;
		ResultSet utente = null;
		try {
			stmt = con.createStatement();
			utente = stmt.executeQuery(query);
			// PreparedStatement ricercaUtente = null;
			// utente = con.prepareStatement("Select * from tabella").executeQuery();
			// utente = ricercaUtente.executeQuery();

			while (utente.next() && vero) {
				String u = utente.getString("user");
				String p = utente.getString("pass");
				int id = utente.getInt("id");
				int accessi = utente.getInt("accessi");
				if (u.equalsIgnoreCase(userID)) {
					if (!p.equals(pass)) {
						passwordSbagliata(response);
						utente.close();
						stmt.close();
						return;
					}
					// Restituisci id e accessi
					restituisciDati(u, p, id, accessi, con, response);
					if (utente != null) {
						utente.close();
						stmt.close();
					}
					return;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (utente != null) {
				try {
					utente.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

		}
		// Creo utente
		creaUtente(userID, pass, con, response);

	}

	public static void passwordSbagliata(HttpServletResponse response) {
		System.out.println("Password errata");
		
		try {
			response.setContentType("text/html");
			PrintWriter writer = null;
			writer = response.getWriter();
			writer.println("<html><body>");
			writer.println("<p><u>Password errata!!</u></p>");
			writer.println("<form>\r\n" + "  <input type=\"button\" value=\"Indietro\" onclick=\"history.back()\">\r\n"
					+ "</form>");
			writer.println("</body></html>");
			
		} catch (Exception e) {
			System.exit(0);
		}

	}

	public static void restituisciDati(String userID, String password, int id, int accessi, Connection con,
			HttpServletResponse response) {
		accessi++;
		PreparedStatement stat = null;
		try {

			stat = con.prepareStatement("UPDATE tabella SET accessi = " + accessi + " WHERE (id = '" + id + "');");
			stat.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stat != null) {
				try {
					stat.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("I tuoi dati sono");
		System.out.println("ID= " + id);
		System.out.println("Username= " + userID);
		System.out.println("Numero di accessi= " + accessi);
		
		try {
			response.setContentType("text/html");
			PrintWriter writer = null;
			writer = response.getWriter();
			writer.println("<html><body>");
			writer.println("<p><u>I tuoi dati sono:</u> <br />");
			writer.println("<br/>");
			writer.println("<br/>");
			writer.println("ID= " + id + "<br/>");
			writer.println("Username= " + userID + "<br/>");
			writer.println("Numero di accessi= " + accessi + "</p>");
			writer.println("</body></html>");
			
			
		} catch (Exception e) {
			System.exit(0);
		} 

	}

	public static void creaUtente(String userID, String password, Connection con, HttpServletResponse response) {
		PreparedStatement state = null;
		try {
			state = con.prepareStatement(
					"INSERT INTO tabella (user, pass, accessi) VALUES (\"" + userID + "\",\"" + password + "\", 0)");
			state.executeUpdate();
			controllaUtente(userID, password, con, response);

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (state != null) {
					state.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public Connection createTable() {
		PreparedStatement create = null;
		try {
			Connection con = getConnection();
			create = con.prepareStatement(
					"CREATE TABLE IF NOT EXISTS tabella(id int NOT NULL AUTO_INCREMENT, user varchar(255), pass varchar(255), accessi int, PRIMARY KEY(id))");
			create.executeUpdate();
			return con;
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			if (create != null) {
				try {
					create.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		return null;
	}

	public Connection getConnection() {
		try {
			String driver = "com.mysql.cj.jdbc.Driver";
			String url = "jdbc:mysql://localhost:3306/location";
			String username = "root";
			String password = "";
			Class.forName(driver);
			// NO FILE READER
			// File file = new
			// File(getClass().getClassLoader().getResource("psw.txt").getFile());
			InputStream in = getClass().getClassLoader().getResourceAsStream("psw.txt");

			Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
			BufferedReader br = new BufferedReader(reader);

			password = br.readLine();
			br.close();
			/*
			 * InputStream in = new FileInputStream(".\\psw.txt"); Reader reader = new
			 * InputStreamReader(in, StandardCharsets.UTF_8); BufferedReader br = new
			 * BufferedReader(reader);
			 * 
			 * password = br.readLine(); br.close();
			 */
			Connection conn = DriverManager.getConnection(url, username, password);
			return conn;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

}
