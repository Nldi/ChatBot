import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

/**
 * Moteur conversationnel principal du Chatbot de culture générale.
 * Gère l'analyse des requêtes utilisateurs, le maintien du contexte discursif
 * et le module d'apprentissage interactif.
 */
public class Chatbot {

    // Constantes textuelles pour le protocole d'interaction utilisateur
    private static final String MESSAGE_IGNORANCE = "Je ne sais pas.";
    private static final String MESSAGE_APPRENTISSAGE = "Je vais te l'apprendre.";
    private static final String MESSAGE_BIENVENUE = "J'attends tes questions de culture générale.";
    private static final String MESSAGE_QUITTER = "Au revoir.";
    private static final String MESSAGE_INVITATION = "Je t'écoute.";
    private static final String MESSAGE_CONFIRMATION = "Très bien, c'est noté.";

    // Structures d'indexation thématique et morphosyntaxique
    private static Index indexThemes;
    private static Index indexFormes;

    // Corpus lexicaux et bases de connaissances
    private static ArrayList<String> motsOutils;
    private static ArrayList<String> reponses;
    private static ArrayList<String> formesReponses;
    private static Thesaurus thesaurus;

    private static final Random generateurAleatoire = new Random();

    public static void main(String[] args) {
        // Chargement et ordonnancement du lexique des mots-outils
        motsOutils = Utilitaire.lireMotsOutils("mots-outils.txt");
        Utilitaire.trierChaines(motsOutils);

        // Chargement du corpus de réponses encyclopédiques
        reponses = Utilitaire.lireReponses("reponses.txt");

        // Initialisation du dictionnaire de normalisation lexicale
        thesaurus = new Thesaurus("thesaurus.txt");

        // Indexation inversée des réponses selon leurs mots porteurs de sens
        indexThemes = Utilitaire.constructionIndexReponses(reponses, motsOutils, thesaurus);

        // Dérivation des formes structurales associées aux réponses
        formesReponses = Utilitaire.constructionTableFormes(reponses, motsOutils, thesaurus);

        // Chargement des paires de référence questions-réponses
        ArrayList<String> questionsReponses = Utilitaire.lireQuestionsReponses("questions-reponses.txt");

        // Construction de l'index liant les schémas de questions aux patrons de réponses
        indexFormes = Utilitaire.constructionIndexFormes(questionsReponses, formesReponses, motsOutils, thesaurus);

        Scanner scannerClavier = new Scanner(System.in);
        String saisie = "";
        String replique;
        String memoireQuestionPrecedente = "";
        String questionNonResolue = "";

        System.out.println();
        System.out.println("> " + MESSAGE_BIENVENUE);

        // Boucle principale d'échange avec l'utilisateur
        while (true) {
            System.out.print("> ");
            if (!scannerClavier.hasNextLine()) {
                break;
            }
            saisie = scannerClavier.nextLine().trim();

            if (saisie.equalsIgnoreCase(MESSAGE_QUITTER)) {
                break;
            }

            // Cas 1 : L'utilisateur enclenche le protocole d'apprentissage
            if (saisie.equals(MESSAGE_APPRENTISSAGE)) {
                if (!questionNonResolue.isEmpty()) {
                    System.out.println("> " + MESSAGE_INVITATION);
                    System.out.print("> ");
                    String reponseEnseignee = scannerClavier.nextLine().trim();

                    // Mémorisation de la réponse si elle n'existe pas encore dans la base
                    if (!Utilitaire.reponseExiste(reponseEnseignee, indexThemes, reponses, motsOutils, thesaurus)) {
                        Utilitaire.IntegrerNouvelleReponse(reponseEnseignee, reponses, indexThemes, motsOutils, thesaurus);
                        Utilitaire.ecrireFichier("reponses.txt", reponseEnseignee);
                    }

                    // Liaison de la forme question-réponse si elle est inédite
                    if (!Utilitaire.formeQuestionReponseExiste(questionNonResolue, reponseEnseignee, indexFormes, formesReponses, motsOutils, thesaurus)) {
                        Utilitaire.integrerNouvelleQuestionReponse(questionNonResolue, reponseEnseignee, formesReponses, indexFormes, motsOutils, thesaurus);
                        Utilitaire.ecrireFichier("questions-reponses.txt", questionNonResolue + " " + reponseEnseignee);
                    }

                    replique = MESSAGE_CONFIRMATION;
                } else {
                    replique = MESSAGE_IGNORANCE;
                }
            }
            // Cas 2 : Question autonome (comporte au moins un mot de contenu sémantique)
            else if (!Utilitaire.entierementInclus(motsOutils, saisie)) {
                replique = repondre(saisie);
                memoireQuestionPrecedente = saisie;
            }
            // Cas 3 : Question anaphorique / de relance (composée uniquement de mots-outils)
            else {
                replique = repondreEnContexte(saisie, memoireQuestionPrecedente);
            }

            // Gestion de l'état d'ignorance pour conditionner l'apprentissage suivant
            if (replique.equals(MESSAGE_IGNORANCE)) {
                questionNonResolue = saisie;
            } else {
                questionNonResolue = "";
            }

            System.out.println("> " + replique);
        }

        scannerClavier.close();
    }

    /**
     * {question non vide et contenant des termes significatifs} =>
     * {résultat = sélection d'une réponse candidate adéquate, ou MESSAGE_IGNORANCE si aucune n'est valide}
     */
    private static String repondre(String question) {
        ArrayList<Integer> candidatesThematiques = Utilitaire.constructionReponsesCandidates(question, indexThemes, motsOutils, thesaurus);

        if (candidatesThematiques.isEmpty()) {
            return MESSAGE_IGNORANCE;
        }

        ArrayList<Integer> selectionFinalisee = Utilitaire.selectionReponsesCandidates(
                question, candidatesThematiques, indexFormes, reponses, formesReponses, motsOutils, thesaurus);

        if (selectionFinalisee.isEmpty()) {
            return MESSAGE_IGNORANCE;
        }

        // Choix pseudo-aléatoire parmi les réponses équivalentes retenues
        int indexChoisi = selectionFinalisee.get(generateurAleatoire.nextInt(selectionFinalisee.size()));
        return reponses.get(indexChoisi);
    }

    /**
     * {question de relance (mots-outils seuls), questionPrecedente question racine} =>
     * {résultat = réponse contextuelle adaptée à la relance, ou MESSAGE_IGNORANCE}
     */
    private static String repondreEnContexte(String question, String questionPrecedente) {
        if (questionPrecedente == null || questionPrecedente.isEmpty()) {
            return MESSAGE_IGNORANCE;
        }

        ArrayList<Integer> candidatsContexte = Utilitaire.constructionReponsesCandidates(
                questionPrecedente, indexThemes, motsOutils, thesaurus);

        if (candidatsContexte.isEmpty()) {
            return MESSAGE_IGNORANCE;
        }

        ArrayList<Integer> filtrees = Utilitaire.selectionReponsesCandidates(
                question, candidatsContexte, indexFormes, reponses, formesReponses, motsOutils, thesaurus);

        if (filtrees.isEmpty()) {
            return MESSAGE_IGNORANCE;
        }

        int tirage = selectionReponseAleatoire(filtrees);
        return reponses.get(tirage);
    }

    /**
     * Sélectionne de manière uniforme un identifiant dans la liste des options valides.
     */
    private static int selectionReponseAleatoire(ArrayList<Integer> options) {
        return options.get(generateurAleatoire.nextInt(options.size()));
    }
}