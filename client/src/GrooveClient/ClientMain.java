package GrooveClient;

import java.util.Scanner;

import GrooveClient.communication.ClientService;

public class ClientMain {
    //main, just print this is client
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        ClientService service = null;
        String line;

        System.out.println("This is client");
        if (args.length != 2) {
            System.err.println("Client requires two arguments: <app_server ip> <app_server port>");
            scanner.close();
            return;
        }

        String appServerIp = args[0];
        int appServerPort = Integer.parseInt(args[1]);

        try {
            System.out.print("Please type your username: ");
			String id = scanner.nextLine();
			System.out.print("Please type your password: ");
			String passString = scanner.nextLine();
            // creates a new client service to send requests
            service = new ClientService(appServerIp, appServerPort, id);
            // performs client registry or login
            System.out.print("Choose whether you want to register (r+Enter) or login (l+Enter): ");
            line = scanner.nextLine();
            if (line.toLowerCase().equals("r")) {
                service.register(id, passString);
            } else if (line.toLowerCase().equals("l")) {
                service.login(id, passString);
            } else {
                System.err.println("Invalid! You typed: " + line);
                return;
            }
            // creates a new parser to read the console input and perform the requests
            CommandParser parser = new CommandParser(service);
            parser.startParsing();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            scanner.close();
            if (service != null) {
                service.shutdownChannel();
            }
        }
    }

}
