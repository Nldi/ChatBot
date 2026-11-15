import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Boîte à outils algorithmique pour le traitement du langage naturel, l'indexation
 * et la recherche de correspondances au sein du chatbot.
 */
public class Utilitaire {

    // Nombre maximal de mots grammaticaux retenus pour modéliser la signature d'une phrase
    private static final int NBMOTS_FORME = 5;

    /**
     * Lit un fichier ligne par ligne en respectant l'encodage UTF-8.
     */
    private static ArrayList<String> lireLignes(String cheminFichier) {
        ArrayList<String> lignes = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(cheminFichier), StandardCharsets.UTF_8))) {
            String courante;
            while ((courante = reader.readLine()) != null) {
                lignes.add(courante);
            }
        } catch (IOException e) {
            System.err.println("Impossible de lire le fichier " + cheminFichier + " : " + e.getMessage());
        }
        return lignes;
    }

    /**
     * {nomFichier valide} =>
     * {résultat = liste des mots-outils extraite du fichier et triée par ordre alphabétique}
     */
    public static ArrayList<String> lireMotsOutils(String nomFichier) {
        ArrayList<String> liste = lireLignes(nomFichier);
        trierChaines(liste);
        return liste;
    }

    /**
     * {nomFichier valide} =>
     * {résultat = liste séquentielle des réponses de culture générale}
     */
    public static ArrayList<String> lireReponses(String nomFichier) {
        return lireLignes(nomFichier);
    }

    /**
     * {nomFichier valide} =>
     * {résultat = liste des exemples d'associations questions/réponses}
     */
    public static ArrayList<String> lireQuestionsReponses(String nomFichier) {
        return lireLignes(nomFichier);
    }

    /**
     * {nomFichier != null, chaineAEcrire != null} =>
     * {ajoute la chaîne en fin de fichier précédée d'un saut de ligne sans écraser le contenu}
     */
    public static void ecrireFichier(String nomFichier, String chaineAEcrire) {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(nomFichier, true), StandardCharsets.UTF_8))) {
            writer.newLine();
            writer.write(chaineAEcrire);
        } catch (IOException e) {
            System.err.println("Échec de l'écriture dans " + nomFichier + " : " + e.getMessage());
        }
    }

    /**
     * {contenu != null} =>
     * {résultat = ensemble ordonné des mots en minuscules débarrassés de la ponctuation usuelle}
     */
    private static ArrayList<String> decoupeEnMots(String contenu) {
        ArrayList<String> mots = new ArrayList<>();
        if (contenu == null || contenu.isEmpty()) {
            return mots;
        }

        char[] tableau = contenu.toLowerCase().toCharArray();
        for (int i = 0; i < tableau.length; i++) {
            char c = tableau[i];
            // Remplacement des séparateurs et signes de ponctuation par des espaces
            if (c == '\n' || c == '\r' || c == '\t' || c == '?' || c == '-' || c == '\'' || c == '’'
                    || c == '.' || c == ',' || c == ':' || c == ';' || c == '"' || c == '(' || c == ')'
                    || c == '«' || c == '»') {
                tableau[i] = ' ';
            }
        }

        String texteNet = new String(tableau);
        for (String segment : texteNet.split(" ")) {
            if (!segment.isEmpty()) {
                mots.add(segment);
            }
        }
        return mots;
    }

    /**
     * Recherche séquentielle d'un terme au sein d'une collection non triée.
     * Complexité : O(n).
     */
    private static boolean existeChaine(ArrayList<String> mots, String mot) {
        for (String element : mots) {
            if (element.equals(mot)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {lesChaines ordonné selon l'ordre lexicographique} =>
     * {résultat = true si chaine est trouvée, false autrement}
     * Algorithme : recherche dichotomique en temps logarithmique O(log n).
     */
    private static boolean existeChaineDicho(ArrayList<String> lesChaines, String chaine) {
        if (lesChaines == null || lesChaines.isEmpty()) {
            return false;
        }

        int bas = 0;
        int haut = lesChaines.size() - 1;

        while (bas <= haut) {
            int milieu = (bas + haut) >>> 1;
            int comparaison = lesChaines.get(milieu).compareTo(chaine);

            if (comparaison == 0) {
                return true;
            } else if (comparaison < 0) {
                bas = milieu + 1;
            } else {
                haut = milieu - 1;
            }
        }

        return false;
    }

    /**
     * {mots trié par ordre alphabétique, question != null} =>
     * {résultat = true si chaque mot de la question figure dans le lexique mots, false sinon}
     */
    public static boolean entierementInclus(ArrayList<String> mots, String question) {
        ArrayList<String> termes = decoupeEnMots(question);
        for (String terme : termes) {
            if (!existeChaineDicho(mots, terme)) {
                return false;
            }
        }
        return true;
    }

    /**
     * {lesChaines != null, chaine != null} =>
     * {résultat = indice de la première occurrence de chaine dans lesChaines, ou -1 si absente}
     */
    private static int rechercherChaine(ArrayList<String> lesChaines, String chaine) {
        for (int idx = 0; idx < lesChaines.size(); idx++) {
            if (lesChaines.get(idx).equals(chaine)) {
                return idx;
            }
        }
        return -1;
    }

    /**
     * {v liste de chaînes non nulle} =>
     * {v est réordonné sur place par ordre lexicographique croissant}
     * Méthode : tri à bulles optimisé avec arrêt précoce dès stabilité.
     */
    static void trierChaines(ArrayList<String> v) {
        if (v == null || v.size() <= 1) {
            return;
        }

        int frontiere = v.size();
        boolean permutation = true;

        while (permutation) {
            permutation = false;
            for (int k = 0; k < frontiere - 1; k++) {
                if (v.get(k).compareTo(v.get(k + 1)) > 0) {
                    String temporaire = v.get(k);
                    v.set(k, v.get(k + 1));
                    v.set(k + 1, temporaire);
                    permutation = true;
                }
            }
            frontiere--;
        }
    }

    /**
     * {v trié par ordre croissant, seuil >= 0} =>
     * {résultat = liste des valeurs ayant la fréquence d'apparition maximale, sous réserve
     * que cette fréquence atteigne ou dépasse le seuil requis}
     */
    static ArrayList<Integer> maxOccurences(ArrayList<Integer> v, int seuil) {
        ArrayList<Integer> selection = new ArrayList<>();
        if (v == null || v.isEmpty()) {
            return selection;
        }

        // Étape 1 : calcul de la multiplicité maximale
        int multipliciteMax = 0;
        int compteur = 1;
        for (int i = 1; i < v.size(); i++) {
            if (v.get(i).equals(v.get(i - 1))) {
                compteur++;
            } else {
                if (compteur > multipliciteMax) {
                    multipliciteMax = compteur;
                }
                compteur = 1;
            }
        }
        if (compteur > multipliciteMax) {
            multipliciteMax = compteur;
        }

        // Si la fréquence maximale n'atteint pas le seuil, aucune valeur n'est éligible
        if (multipliciteMax < seuil) {
            return selection;
        }

        // Étape 2 : extraction des identifiants atteignant cette multiplicité maximale
        compteur = 1;
        for (int i = 1; i < v.size(); i++) {
            if (v.get(i).equals(v.get(i - 1))) {
                compteur++;
            } else {
                if (compteur == multipliciteMax) {
                    selection.add(v.get(i - 1));
                }
                compteur = 1;
            }
        }
        if (compteur == multipliciteMax) {
            selection.add(v.get(v.size() - 1));
        }

        return selection;
    }

    /**
     * {v1 et v2 sont des listes triées par ordre croissant} =>
     * {résultat = nouvelle liste fusionnée ordonnée contenant tous les éléments avec répétitions}
     * Algorithme d'interclassement linéaire en O(|v1| + |v2|).
     */
    static ArrayList<Integer> fusion(ArrayList<Integer> v1, ArrayList<Integer> v2) {
        ArrayList<Integer> resultat = new ArrayList<>(v1.size() + v2.size());
        int curseur1 = 0;
        int curseur2 = 0;

        while (curseur1 < v1.size() && curseur2 < v2.size()) {
            int val1 = v1.get(curseur1);
            int val2 = v2.get(curseur2);

            if (val1 <= val2) {
                resultat.add(val1);
                curseur1++;
            } else {
                resultat.add(val2);
                curseur2++;
            }
        }

        while (curseur1 < v1.size()) {
            resultat.add(v1.get(curseur1++));
        }
        while (curseur2 < v2.size()) {
            resultat.add(v2.get(curseur2++));
        }

        return resultat;
    }

    /**
     * {s chaîne de caractères non nulle} =>
     * {résultat = true si s est uniquement composée de chiffres [0-9], false sinon}
     */
    public static boolean estUnNombre(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    /**
     * {chaine != null, motsOutils trié, thesaurus initialisé} =>
     * {résultat = empreinte structurale de la phrase composée des NBMOTS_FORME premiers mots-outils
     * indexés par leur position, les nombres étant abstraitisés en "NUM"}
     */
    static String calculForme(String chaine, ArrayList<String> motsOutils, Thesaurus thesaurus) {
        ArrayList<String> mots = decoupeEnMots(chaine);
        StringBuilder empreinte = new StringBuilder();
        int motsOutilsComptes = 0;

        for (int i = 0; i < mots.size() && motsOutilsComptes < NBMOTS_FORME; i++) {
            String terme = mots.get(i);

            // Normalisation des valeurs numériques en motif générique
            if (estUnNombre(terme)) {
                terme = "NUM";
                mots.set(i, terme);
            }

            if (terme.equals("NUM") || existeChaineDicho(motsOutils, terme)) {
                empreinte.append(terme).append('_').append(i).append(' ');

                // Intégration de la variante lemmatisée du thésaurus
                String lemme = thesaurus.rechercherSortiePourEntree(terme);
                if (!lemme.equals(terme)) {
                    empreinte.append(lemme).append('_').append(i).append(' ');
                }
                motsOutilsComptes++;
            }
        }

        return empreinte.toString();
    }

    /**
     * {reponses != null, motsOutils trié, thesaurus != null} =>
     * {résultat = liste des formes structurales associées à chaque réponse}
     */
    public static ArrayList<String> constructionTableFormes(ArrayList<String> reponses, ArrayList<String> motsOutils, Thesaurus thesaurus) {
        ArrayList<String> formes = new ArrayList<>(reponses.size());
        for (String rep : reponses) {
            formes.add(calculForme(rep, motsOutils, thesaurus));
        }
        return formes;
    }

    /**
     * Construit l'index inversé associant chaque mot-clé porteur de sens d'une réponse
     * (ainsi que ses éventuels synonymes) à l'identifiant de cette réponse.
     */
    public static Index constructionIndexReponses(ArrayList<String> reponses, ArrayList<String> motsOutils, Thesaurus thesaurus) {
        Index indexResultat = new Index();
        if (reponses == null || motsOutils == null) {
            return indexResultat;
        }

        for (int idReponse = 0; idReponse < reponses.size(); idReponse++) {
            ArrayList<String> tokens = decoupeEnMots(reponses.get(idReponse));

            for (String token : tokens) {
                // On n'indexe pas les mots-outils dans l'index thématique
                if (!existeChaineDicho(motsOutils, token)) {
                    indexResultat.ajouterSortieAEntree(token, idReponse);

                    // Extension sémantique via le thésaurus
                    String syn = thesaurus.rechercherSortiePourEntree(token);
                    if (!syn.equals(token)) {
                        indexResultat.ajouterSortieAEntree(syn, idReponse);
                    }
                }
            }
        }

        return indexResultat;
    }

    /**
     * Construit l'index associant les mots-outils positionnés des questions aux formes de réponses attendues.
     */
    public static Index constructionIndexFormes(ArrayList<String> questionsReponses, ArrayList<String> formes, ArrayList<String> motsOutils, Thesaurus thesaurus) {
        Index indexResultat = new Index();
        if (questionsReponses == null || formes == null) {
            return indexResultat;
        }

        for (String paireQR : questionsReponses) {
            int posSeparateur = paireQR.indexOf('?');
            if (posSeparateur < 0) {
                continue;
            }

            String question = paireQR.substring(0, posSeparateur).trim();
            String reponse = paireQR.substring(posSeparateur + 1).trim();

            String formeRep = calculForme(reponse, motsOutils, thesaurus);
            int idForme = rechercherChaine(formes, formeRep);

            if (idForme >= 0) {
                ArrayList<String> motifsQuestion = decoupeEnMots(calculForme(question, motsOutils, thesaurus));
                int nbAConsiderer = Math.min(motifsQuestion.size(), NBMOTS_FORME);

                for (int pos = 0; pos < nbAConsiderer; pos++) {
                    indexResultat.ajouterSortieAEntree(motifsQuestion.get(pos), idForme);
                }
            }
        }

        return indexResultat;
    }

    /**
     * Détermine les identifiants de réponses candidates couvrant la totalité des mots significatifs de la question.
     */
    public static ArrayList<Integer> constructionReponsesCandidates(String question, Index indexReponses, ArrayList<String> motsOutils, Thesaurus thesaurus) {
        ArrayList<Integer> cumulCandidates = new ArrayList<>();
        ArrayList<String> motsQuestion = decoupeEnMots(question);
        int totalMotsSignificatifs = 0;

        for (String mot : motsQuestion) {
            if (!existeChaineDicho(motsOutils, mot)) {
                String formeNormalisee = thesaurus.rechercherSortiePourEntree(mot);
                ArrayList<Integer> matches = indexReponses.rechercherSorties(formeNormalisee);
                cumulCandidates = fusion(cumulCandidates, matches);
                totalMotsSignificatifs++;
            }
        }

        // Ne retient que les réponses indexées pour tous les mots clés de la requête
        return maxOccurences(cumulCandidates, totalMotsSignificatifs);
    }

    /**
     * Filtre les réponses candidates pour ne garder que celles dont la syntaxe et le type
     * (temporel/numérique ou nominatif) correspondent à la forme de la question.
     */
    public static ArrayList<Integer> selectionReponsesCandidates(String question, ArrayList<Integer> candidates, Index indexFormes, ArrayList<String> reponses, ArrayList<String> formesReponses, ArrayList<String> motsOutils, Thesaurus thesaurus) {
        ArrayList<String> motifsQuestion = decoupeEnMots(calculForme(question, motsOutils, thesaurus));
        ArrayList<Integer> formesCompatibles = new ArrayList<>();

        int borne = Math.min(motifsQuestion.size(), NBMOTS_FORME);
        for (int i = 0; i < borne; i++) {
            String jeton = motifsQuestion.get(i);
            int idxSeparateur = jeton.lastIndexOf('_');

            String motPur = jeton.substring(0, idxSeparateur);
            String position = jeton.substring(idxSeparateur);

            formesCompatibles = fusion(formesCompatibles, indexFormes.rechercherSorties(jeton));

            String formeCanonique = thesaurus.rechercherSortiePourEntree(motPur);
            if (!formeCanonique.equals(motPur)) {
                formesCompatibles = fusion(formesCompatibles, indexFormes.rechercherSorties(formeCanonique + position));
            }
        }

        formesCompatibles = maxOccurences(formesCompatibles, borne);

        // Constitution du lexique des formes valides
        ArrayList<String> lexiqueFormesValides = new ArrayList<>();
        for (Integer idForme : formesCompatibles) {
            String forme = formesReponses.get(idForme);
            if (!existeChaine(lexiqueFormesValides, forme)) {
                lexiqueFormesValides.add(forme);
            }
        }
        trierChaines(lexiqueFormesValides);

        // Détection d'une attente numérique (date, année)
        String qMin = question.toLowerCase();
        boolean exigenceNombre = qMin.contains("année") || qMin.contains("annee") || qMin.contains("quand");

        ArrayList<Integer> validees = new ArrayList<>();
        for (Integer idCandidate : candidates) {
            String formeCandidate = (idCandidate < formesReponses.size()) ? formesReponses.get(idCandidate) : "";

            if (existeChaineDicho(lexiqueFormesValides, formeCandidate)) {
                if (!exigenceNombre) {
                    validees.add(idCandidate);
                } else {
                    // Vérification de la présence effective d'une valeur numérique dans la réponse
                    ArrayList<String> motsRep = decoupeEnMots(reponses.get(idCandidate));
                    boolean contientChiffre = false;
                    for (String w : motsRep) {
                        if (estUnNombre(w)) {
                            contientChiffre = true;
                            break;
                        }
                    }
                    if (contientChiffre) {
                        validees.add(idCandidate);
                    }
                }
            }
        }

        return validees;
    }

    /**
     * Vérifie par consultation d'index si la réponse donnée existe textuellement dans le corpus.
     */
    public static boolean reponseExiste(String reponse, Index indexReponses, ArrayList<String> reponses, ArrayList<String> motsOutils, Thesaurus thesaurus) {
        ArrayList<String> mots = decoupeEnMots(reponse);
        ArrayList<Integer> indicesCandidates = new ArrayList<>();
        int seuilMots = 0;

        for (String m : mots) {
            if (!existeChaineDicho(motsOutils, m)) {
                String lemme = thesaurus.rechercherSortiePourEntree(m);
                indicesCandidates = fusion(indicesCandidates, indexReponses.rechercherSorties(lemme));
                seuilMots++;
            }
        }

        indicesCandidates = maxOccurences(indicesCandidates, seuilMots);

        for (Integer idx : indicesCandidates) {
            if (reponses.get(idx).equals(reponse)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Vérifie si l'association syntaxique entre la question et la réponse est déjà référencée.
     */
    public static boolean formeQuestionReponseExiste(String question, String reponse, Index indexFormes, ArrayList<String> formesReponses, ArrayList<String> motsOutils, Thesaurus thesaurus) {
        ArrayList<String> motifsQuestion = decoupeEnMots(calculForme(question, motsOutils, thesaurus));
        String formeAttendue = calculForme(reponse, motsOutils, thesaurus);
        ArrayList<Integer> idsFormes = new ArrayList<>();

        int seuil = Math.min(motifsQuestion.size(), NBMOTS_FORME);
        for (int i = 0; i < seuil; i++) {
            String jeton = motifsQuestion.get(i);
            idsFormes = fusion(indexFormes.rechercherSorties(jeton), idsFormes);

            int separateur = jeton.lastIndexOf('_');
            if (separateur != -1) {
                String mot = jeton.substring(0, separateur);
                String pos = jeton.substring(separateur);
                String syn = thesaurus.rechercherSortiePourEntree(mot);
                if (!syn.equals(mot)) {
                    idsFormes = fusion(indexFormes.rechercherSorties(syn + pos), idsFormes);
                }
            }
        }

        idsFormes = maxOccurences(idsFormes, seuil);

        for (Integer id : idsFormes) {
            if (id >= 0 && id < formesReponses.size() && formesReponses.get(id).equals(formeAttendue)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Intègre dynamiquement une nouvelle réponse au corpus et actualise l'index thématique.
     */
    public static void IntegrerNouvelleReponse(String reponse, ArrayList<String> reponses, Index indexContenu, ArrayList<String> motsOutils, Thesaurus thesaurus) {
        reponses.add(reponse);
        int nouvelIndex = reponses.size() - 1;

        ArrayList<String> mots = decoupeEnMots(reponse);
        for (String mot : mots) {
            if (!existeChaineDicho(motsOutils, mot)) {
                indexContenu.ajouterSortieAEntree(mot, nouvelIndex);

                String canonique = thesaurus.rechercherSortiePourEntree(mot);
                if (!canonique.equals(mot)) {
                    indexContenu.ajouterSortieAEntree(canonique, nouvelIndex);
                }
            }
        }
    }

    /**
     * Intègre une nouvelle association question-réponse en enrichissant la table des formes et l'index associé.
     */
    public static void integrerNouvelleQuestionReponse(String question, String reponse, ArrayList<String> formes, Index indexFormes, ArrayList<String> motsOutils, Thesaurus thesaurus) {
        String formeRep = calculForme(reponse, motsOutils, thesaurus);
        int indexForme = rechercherChaine(formes, formeRep);

        if (indexForme == -1) {
            formes.add(formeRep);
            indexForme = formes.size() - 1;
        }

        String formeQ = calculForme(question, motsOutils, thesaurus);
        ArrayList<String> tokensQ = decoupeEnMots(formeQ);
        int limite = Math.min(tokensQ.size(), NBMOTS_FORME);

        for (int i = 0; i < limite; i++) {
            String jeton = tokensQ.get(i);
            indexFormes.ajouterSortieAEntree(jeton, indexForme);

            int sep = jeton.lastIndexOf('_');
            if (sep != -1) {
                String racine = jeton.substring(0, sep);
                String suffixe = jeton.substring(sep);
                String syn = thesaurus.rechercherSortiePourEntree(racine);
                if (!syn.equals(racine)) {
                    indexFormes.ajouterSortieAEntree(syn + suffixe, indexForme);
                }
            }
        }
    }
}
