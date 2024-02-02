package CryptographicLibrary.cli;

public class CryptoLibCliMain {
    
    public static void main(String[] args) throws Exception{
        if(args.length == 0){
            CryptographicLibrary.cli.commands.HelpCommand.main();
            return;
        }
        
        final String command = args[0];
        final String[] newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, args.length - 1);

        switch(command){
            case "protect":
                 CryptographicLibrary.cli.commands.ProtectCommand.main(newArgs);
                break;
            case "unprotect":
                 CryptographicLibrary.cli.commands.UnprotectCommand.main(newArgs);
                break;
            case "check":
                 CryptographicLibrary.cli.commands.CheckCommand.main(newArgs);
                break;
            case "generate-key":
                 CryptographicLibrary.cli.commands.GenerateKeyCommand.main(newArgs);
                 break;
            default:
                 CryptographicLibrary.cli.commands.HelpCommand.main();
                break;
        }
    }
}
