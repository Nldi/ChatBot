import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Gestionnaire de thésaurus permettant de normaliser les mots vers leur lemme ou forme canonique.
 * Exemple : "appelle" -> "nomme", "la" -> "le".
 */
public class Thesaurus {

    /**
     * Représente une association unidirectionnelle entre une forme lexicale et sa forme canonique.
     */
    private class EntreeSortie implements Comparable<EntreeSortie> {
        private String entree;
        private String sortie;

        public EntreeSortie(String entree, String sortie) {
            this.entree = entree;
            this.sortie = sortie;
        }

        @Override
        public int compareTo(EntreeSortie autre) {
            return this.entree.compareTo(autre.entree);
        }

        @Override
        public String toString() {
            return entree + " -> " + sortie;
        }
    }

    private ArrayList<EntreeSortie> table;

    /**
     * {nomFichier désigne un fichier texte valide avec des paires entree:sortie} =>
     * {la table est remplie et triée selon l'ordre lexicographique des entrées}
     */
    public Thesaurus(String nomFichier) {
        this.table = new ArrayList<>();

        try (BufferedReader lecteur = new BufferedReader(
                new InputStreamReader(new FileInputStream(nomFichier), StandardCharsets.UTF_8))) {
            
            String ligne;
            while ((ligne = lecteur.readLine()) != null) {
                ligne = ligne.trim();
                int separateur = ligne.indexOf(':');
                if (separateur > 0 && separateur < ligne.length() - 1) {
                    String cle = ligne.substring(0, separateur);
                    String valeur = ligne.substring(separateur + 1);
                    ajouterEntreeSortie(cle, valeur);
                }
            }
            // Maintien de l'invariant d'ordre nécessaire à la recherche binaire
            trierEntreesSorties(this.table);

        } catch (IOException e) {
            System.err.println("Impossible de charger le thésaurus depuis " + nomFichier + " : " + e.getMessage());
        }
    }

    /**
     * {entree != null, sortie != null} =>
     * {ajoute le couple (entree, sortie) en fin de liste}
     */
    public void ajouterEntreeSortie(String entree, String sortie) {
        this.table.add(new EntreeSortie(entree, sortie));
    }

    /**
     * {table triée par rapport au champ entree} =>
     * {résultat = la forme canonique normalisée si trouvée, ou la chaîne entree inchangée}
     * Complexité : O(log n) par recherche dichotomique.
     */
    public String rechercherSortiePourEntree(String entree) {
        if (table == null || table.isEmpty()) {
            return entree;
        }

        int bas = 0;
        int haut = table.size() - 1;

        while (bas <= haut) {
            int centre = (bas + haut) >>> 1;
            int comparaison = table.get(centre).entree.compareTo(entree);

            if (comparaison == 0) {
                return table.get(centre).sortie;
            } else if (comparaison < 0) {
                bas = centre + 1;
            } else {
                haut = centre - 1;
            }
        }

        return entree;
    }

    /**
     * {v liste non nulle d'EntreeSortie} =>
     * {v est ordonné par ordre lexicographique croissant sur l'attribut entree}
     * Algorithme : tri à bulles avec arrêt anticipé en O(n) au meilleur cas et O(n²) au pire.
     */
    static void trierEntreesSorties(ArrayList<EntreeSortie> v) {
        if (v == null || v.size() <= 1) {
            return;
        }

        int limite = v.size();
        boolean modification = true;

        while (modification) {
            modification = false;
            for (int k = 0; k < limite - 1; k++) {
                if (v.get(k).compareTo(v.get(k + 1)) > 0) {
                    EntreeSortie temporaire = v.get(k);
                    v.set(k, v.get(k + 1));
                    v.set(k + 1, temporaire);
                    modification = true;
                }
            }
            limite--;
        }
    }

    /**
     * Affichage tabulaire des équivalences du thésaurus.
     */
    public void afficher() {
        for (EntreeSortie element : table) {
            System.out.println(element);
        }
    }
}
