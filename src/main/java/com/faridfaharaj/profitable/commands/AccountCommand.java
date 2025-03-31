package com.faridfaharaj.profitable.commands;

import com.faridfaharaj.profitable.Configuration;
import com.faridfaharaj.profitable.data.tables.Accounts;
import com.faridfaharaj.profitable.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class AccountCommand implements CommandExecutor {


    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        Player player = null;
        if(sender instanceof Player got){
            player = got;
        }

        if(args.length == 0){
            if(player != null){
                String account = Accounts.getAccount(player);
                TextUtil.sendCustomMessage(sender, Component.text("").append(TextUtil.profitablePrefix()).append(Component.text("Account: "))
                        .append((Objects.equals(account, player.getUniqueId().toString()) ?
                        Component.text("(Default)").color(Configuration.COLOREMPTY):
                        Component.text(account).color(Configuration.COLORINFO))));
                return true;
            }
            return false;
        }

        //BASIC ACCOUNT COMMANDS
        if(args[0].equals("register")){

            if(!sender.hasPermission("profitable.account.manage.register")){
                TextUtil.sendGenericMissingPerm(sender);
                return true;
            }

            if(args.length < 4){
                TextUtil.sendError(sender, "/account register <Username> <Password> <Repeat Password>");
                return true;
            }

            if(Objects.equals(args[2], args[3])){
                if(Accounts.registerAccount(args[1], args[2])){
                    TextUtil.sendSuccsess(sender, "Account " + args[1] + " registered successfully");
                    return true;
                }else{
                    TextUtil.sendError(sender, "Account already exists");
                    return true;
                }
            }else {
                TextUtil.sendError(sender, "Passwords don't match");
                return true;
            }

        }

        if(args[0].equals("delete")){

            if(!sender.hasPermission("profitable.account.manage.delete")){
                TextUtil.sendGenericMissingPerm(sender);
                return true;
            }

            if(args.length < 3){
                TextUtil.sendError(sender, "/account delete <Account> <password>");
                return true;
            }

            if(player != null){
                String account = args[1];
                UUID playerid = player.getUniqueId();

                if(Objects.equals(playerid.toString(), args[2])){
                    TextUtil.sendError(sender, "Cannot delete default account");
                }

                if(Objects.equals(account, Accounts.getAccount(player))){

                    if(!Accounts.comparePasswords(account, args[2])){
                        TextUtil.sendError(sender, "Passwords don't match");
                        return true;
                    }

                    TextUtil.sendWarning(sender, "logged out of active account");
                    Accounts.logOut(playerid);

                    if(Accounts.getCurrentAccounts().containsValue(account)) {
                        TextUtil.sendError(sender, "Someone else is still using this account, cannot delete");
                    }else{
                        Accounts.deleteAccount(account);
                        TextUtil.sendCustomMessage(sender, TextUtil.profitablePrefix().append(Component.text("DELETED account: " + account).color(NamedTextColor.RED)));
                    }
                    return true;

                }else{
                    TextUtil.sendError(sender, "Account name doesn't match with active account's");
                }

                return true;

            }else{
                TextUtil.sendError(sender, "cannot run this from console");
                return true;
            }

        }

        if(args[0].equals("login")){

            if(!sender.hasPermission("profitable.account.manage.login")){
                TextUtil.sendGenericMissingPerm(sender);
                return true;
            }

            if(args.length < 3){
                TextUtil.sendError(sender, "/account login <Account> <Password>");
                return true;
            }

            if(player != null){

                if(Objects.equals(args[1], Accounts.getCurrentAccounts().get(player.getUniqueId()))){
                    TextUtil.sendError(sender, "Already logged in");
                    return true;
                }

                if(Accounts.logIn(player.getUniqueId(), args[1], args[2])){
                    TextUtil.sendSuccsess(sender,"successfully logged into " + args[1]);
                }else{
                    TextUtil.sendError(sender, "Incorrect account or password");
                }
                return true;
            }else{
                TextUtil.sendGenericCantConsole(sender);
            }

        }

        if(args[0].equals("logout")){

            if(!sender.hasPermission("profitable.account.manage.logout")){
                TextUtil.sendGenericMissingPerm(sender);
                return true;
            }

            if(player != null){
                UUID playerid = player.getUniqueId();
                if(!Accounts.getCurrentAccounts().containsKey(playerid)){
                    TextUtil.sendError(sender, "No active account found");
                    return true;
                }

                Accounts.logOut(playerid);

                TextUtil.sendSuccsess(sender,"Logged out successfully");
                return  true;

            }

        }

        if(args[0].equals("password")){

            if(!sender.hasPermission("profitable.account.manage.password")){
                TextUtil.sendGenericMissingPerm(sender);
                return true;
            }

            if(args.length < 3){
                TextUtil.sendError(sender,"/account password <Old password> <New password>");
                return true;
            }

            if(player != null){
                String account = Accounts.getAccount(player);
                if(Accounts.comparePasswords(account, args[1])){
                    Accounts.changePassword(account, args[2]);
                    TextUtil.sendSuccsess(sender, "Password updated successfully");
                }else{
                    TextUtil.sendError(sender,"Incorrect password");
                }
            }else{
                TextUtil.sendGenericCantConsole(sender);
            }
            return true;
        }



        TextUtil.sendError(sender, "Invalid Subcommand");
        return true;
    }

    public static class CommandTabCompleter implements TabCompleter {

        @Override
        public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {

            List<String> suggestions = new ArrayList<>();

            if(args.length == 1){
                List<String> options = new ArrayList<>(List.of("register", "login", "logout", "password", "delete"));

                StringUtil.copyPartialMatches(args[0], options, suggestions);
            }

            if(args.length >= 2){

                if(Objects.equals(args[0], "register")){
                    if(args.length == 2){
                        suggestions = List.of("[<Account>]");
                    }else if(args.length == 3){
                        suggestions = List.of("[<Password>]");
                    }else if(args.length == 4){
                        suggestions = List.of("[<Repeat password>]");
                    }
                }

                if(Objects.equals(args[0], "delete")){
                    if(args.length == 2){
                        suggestions = List.of("[<Account>]");
                    }else if(args.length == 3){
                        suggestions = List.of("[<Password>]");
                    }
                }

                if(Objects.equals(args[0], "login")){
                    if(args.length == 2){
                        suggestions = List.of("[<Account>]");
                    }else if(args.length == 3){
                        suggestions = List.of("[<Password>]");
                    }
                }

                if(Objects.equals(args[0], "password")){
                    if(args.length == 2){
                        suggestions = List.of("[<Old password>]");
                    }else{
                        suggestions = List.of("[<New password>]");
                    }
                }
            }

            return suggestions;
        }

    }
}
