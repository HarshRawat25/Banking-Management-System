import ui.BankingUI;
import service.Bank;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Bank bank = new Bank();
            BankingUI ui = new BankingUI(bank);
            ui.setVisible(true);
            Runtime.getRuntime().addShutdownHook(new Thread(ui::stopBackground));
        });
    }
}
