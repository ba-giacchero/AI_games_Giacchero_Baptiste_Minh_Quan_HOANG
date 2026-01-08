import controllers.RuleController;
import java.io.*;
import java.util.concurrent.*;
import models.Board;
import models.MoveCommand;
import models.MoveFactory;
import models.Player;

public class Arbitre {
    private static final int TIMEOUT_SECONDS = 3;

    public static void main(String[] args) throws Exception {
        // On réutilise le même classpath que celui ayant servi à lancer Arbitre,
        // pour être sûr que JoueurExterne et tous les packages controllers/models
        // sont bien trouvés dans les processus enfants.
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        String classpath = System.getProperty("java.class.path");

        // A et B pointent tous les deux sur JoueurExterne (même code, deux processus séparés)
        Process A = Runtime.getRuntime().exec(
                new String[] { javaBin, "-cp", classpath, "JoueurExterne", "JoueurA" });
        //        Process A = new ProcessBuilder("./A").start();
        // Pour lancer un code java COMPILE : voir https://www.baeldung.com/java-process-api
        // process = Runtime.getRuntime().exec("java -cp src/main/java com.baeldung.java9.process.OutputStreamExample");
        // sinon il faut lancer la compil avant (puis lancer le code compilé):
//        Process process = Runtime.getRuntime().exec("javac -cp src src\\main\\java\\com\\baeldung\\java9\\process\\OutputStreamExample.java");
        Process B = Runtime.getRuntime().exec(
                new String[] { javaBin, "-cp", classpath, "JoueurExterne", "JoueurB" });
        //Process B = new ProcessBuilder("./B").start();

        Joueur joueurA = new Joueur("A", A);
        Joueur joueurB = new Joueur("B", B);

        // Etat interne de la partie (plateau + scores) selon les règles 2025
        Board board = new Board();
        Player player1 = new Player(0, "Player 1");
        Player player2 = new Player(1, "Player 2");
        Player[] players = { player1, player2 };
        RuleController ruleController = new RuleController();
        // MoveFactory en mode silencieux : seule la ligne "A -> 13B" etc. est affichée par l'arbitre.
        MoveFactory moveFactory = new MoveFactory(board, ruleController, players, true);

        Joueur courant = joueurA;
        Joueur autre = joueurB;

        String coup = "START";
        int nbCoups = 0;
        while (true) {
            // Reception du coup de l'adversaire
            courant.receive(coup);
            // reponse avec TIMEOUT
            coup = courant.response(TIMEOUT_SECONDS);
            if (coup == null) {
                // Timeout : on termine la partie, en signalant TIMEOUT
                int s1 = player1.getCaptured();
                int s2 = player2.getCaptured();
                System.out.println("RESULT TIMEOUT " + s1 + " " + s2);
                break;
            }
            nbCoups++;

            // Validation et application du coup sur le plateau interne
            int currentIndex = (courant == joueurA) ? 0 : 1;
            Player currentPlayer = players[currentIndex];

            MoveCommand cmd = moveFactory.createMove(coup, currentPlayer);
            if (cmd == null || !cmd.execute()) {
                // Coup invalide : on termine en le signalant
                int s1 = player1.getCaptured();
                int s2 = player2.getCaptured();
                System.out.println("RESULT INVALID " + s1 + " " + s2);
                break;
            }

            System.out.println(courant.nom + " -> " + coup);

            // Fin de partie par règles (captures ou < 10 graines)
            if (ruleController.isGameOver(board, players)) {
                int s1 = player1.getCaptured();
                int s2 = player2.getCaptured();
                System.out.println("RESULT " + coup + " " + s1 + " " + s2);
                break;
            }

            // Limite de 400 coups au total (un coup = un joueur)
            if (nbCoups == 400) {
                int s1 = player1.getCaptured();
                int s2 = player2.getCaptured();
                System.out.println("RESULT LIMIT " + s1 + " " + s2);
                break;
            }
            // Changement de joueur
            Joueur tmp = courant;
            courant = autre;
            autre = tmp;
        }
        joueurA.destroy();
        joueurB.destroy();
        System.out.println("Fin.");
    }

    static class Joueur {
        String nom;
        Process process;
        BufferedWriter in;
        BufferedReader out;
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Joueur(String nom, Process p) {
            this.nom = nom;
            this.process = p;
            this.in = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
            this.out = new BufferedReader(new InputStreamReader(p.getInputStream()));
        }

        void receive(String msg) throws IOException {
            in.write(msg);
            in.newLine();
            in.flush();
        }
        String response(int timeoutSeconds) throws IOException {
            Future<String> future = executor.submit(() -> out.readLine());
            try {
                return future.get(timeoutSeconds, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                future.cancel(true);
                return null;
            } catch (Exception e) {
                return null;
            }
        }

        void destroy() {
            executor.shutdownNow();
            process.destroy();
        }
    }
}
