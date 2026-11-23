package use_case.view_stats;

import java.awt.BorderLayout;
import data_access.FileGameHistoryGateway;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import main.interface_adapters.view_stats.ViewStatsController;
import main.interface_adapters.view_stats.ViewStatsPresenter;

public class ViewStatsSwingDemo {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ViewStatsSwingDemo::showWindow);
    }

    private static void showWindow() {
        JLabel resultLabel = new JLabel("Click My Statistics to see your results.");
        JButton statsButton = new JButton("My Statistics");

        Path file = Path.of(System.getProperty("user.home"),
                "poker-stats-demo", "game-history.csv");
        GameHistoryGateway history = new FileGameHistoryGateway(file);

        ViewStatsPresenter presenter = new ViewStatsPresenter(message ->
                SwingUtilities.invokeLater(() -> resultLabel.setText(message)));
        ViewStatsInteractor interactor = new ViewStatsInteractor(history, presenter);
        ViewStatsController controller = new ViewStatsController(interactor);

        statsButton.addActionListener(event -> {
            statsButton.setEnabled(false);
            CompletableFuture.runAsync(() -> controller.viewStats("player-1"))
                    .whenCompleteAsync((unused, error) -> {
                        statsButton.setEnabled(true);
                        if (error != null) {
                            presenter.presentHistoryUnavailable();
                        }
                    }, SwingUtilities::invokeLater);
        });

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.add(statsButton, BorderLayout.NORTH);
        panel.add(resultLabel, BorderLayout.CENTER);

        JFrame window = new JFrame("Poker Statistics");
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        window.setContentPane(panel);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }
}
