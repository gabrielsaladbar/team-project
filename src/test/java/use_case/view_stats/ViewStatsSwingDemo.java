package use_case.view_stats;

import java.awt.BorderLayout;
import java.util.List;
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

        GameHistoryGateway sampleHistory = (playerId, limit) -> List.of(
                GameHistoryGateway.Outcome.WIN,
                GameHistoryGateway.Outcome.LOSS,
                GameHistoryGateway.Outcome.TIE
        );

        ViewStatsPresenter presenter = new ViewStatsPresenter(resultLabel::setText);
        ViewStatsInteractor interactor = new ViewStatsInteractor(sampleHistory, presenter);
        ViewStatsController controller = new ViewStatsController(interactor);

        statsButton.addActionListener(event -> controller.viewStats("player-1"));

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
