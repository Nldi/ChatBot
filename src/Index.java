import java.util.ArrayList;

/**
 * Structure de données représentant un index inversé ordonné.
 * Associe des clés textuelles (termes, formes de phrases) à une liste triée
 * d'identifiants entiers uniques (indices de réponses).
 */
public class Index {

    /**
     * Entrée élémentaire associant un mot-clé à sa liste d'identifiants ordonnée.
     */
    private class EntreeIndex {
        private String cle;
        private ArrayList<Integer> sorties;

        public EntreeIndex(String cle) {
            this.cle = cle;
            this.sorties = new ArrayList<>();
        }

        public ArrayList<Integer> getSorties() {
            return this.sorties;
        }

        /**
         * {sorties ordonné par ordre croissant} =>
         * {résultat = indice de l'élément s'il est présent, -(indice_insertion + 1) sinon}
         * Complexité : O(log k) où k est la taille de la liste des sorties.
         */
        public int rechercherSortie(Integer sortie) {
            int gauche = 0;
            int droite = sorties.size() - 1;

            while (gauche <= droite) {
                int milieu = (gauche + droite) >>> 1;
                int valeur = sorties.get(milieu);

                if (valeur == sortie.intValue()) {
                    return milieu;
                } else if (valeur < sortie) {
                    gauche = milieu + 1;
                } else {
                    droite = milieu - 1;
                }
            }

            return -(gauche + 1);
        }

        /**
         * {sortie est un entier non null} =>
         * {insère sortie à sa position ordonnée sans créer de doublon}
         */
        public void ajouterSortie(Integer sortie) {
            int localisation = rechercherSortie(sortie);
            // On n'ajoute que si l'identifiant n'est pas déjà présent
            if (localisation < 0) {
                int indexInsertion = -(localisation + 1);
                sorties.add(indexInsertion, sortie);
            }
        }

        @Override
        public String toString() {
            return cle + " => " + sorties;
        }
    }

    // Table contenant les entrées indexées, maintenue triée par ordre alphabétique
    private ArrayList<EntreeIndex> table;

    public Index() {
        this.table = new ArrayList<>();
    }

    /**
     * {table triée par ordre lexicographique} =>
     * {résultat = rang de l'entrée dans la table si existante, -(rang_insertion + 1) sinon}
     * Algorithme : recherche dichotomique en O(log N).
     */
    public int rechercherEntree(String entree) {
        int debut = 0;
        int fin = table.size() - 1;

        while (debut <= fin) {
            int pivot = (debut + fin) >>> 1;
            int diff = table.get(pivot).cle.compareTo(entree);

            if (diff == 0) {
                return pivot;
            } else if (diff < 0) {
                debut = pivot + 1;
            } else {
                fin = pivot - 1;
            }
        }

        return -(debut + 1);
    }

    /**
     * {entree non nulle, sortie non nulle} =>
     * {associe la valeur sortie à l'entrée demandée ; l'entrée est créée si inexistante}
     */
    public void ajouterSortieAEntree(String entree, Integer sortie) {
        int position = rechercherEntree(entree);

        if (position >= 0) {
            // L'entrée existe déjà, on lui rattache le nouvel identifiant
            table.get(position).ajouterSortie(sortie);
        } else {
            // Entrée inconnue : création et insertion à l'emplacement préservant l'ordre
            int pointInsertion = -(position + 1);
            EntreeIndex nouvelleEntree = new EntreeIndex(entree);
            nouvelleEntree.ajouterSortie(sortie);
            table.add(pointInsertion, nouvelleEntree);
        }
    }

    /**
     * {entree non nulle} =>
     * {résultat = la liste des identifiants liés à cette entrée, ou une liste vide si absente}
     */
    public ArrayList<Integer> rechercherSorties(String entree) {
        int index = rechercherEntree(entree);
        if (index >= 0) {
            return table.get(index).getSorties();
        }
        return new ArrayList<>();
    }

    /**
     * Affiche l'intégralité du contenu de l'index dans la sortie standard.
     */
    public void afficher() {
        for (EntreeIndex entree : table) {
            System.out.println(entree);
        }
    }
}
