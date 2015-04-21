package agent.planningagent;

import environnement.Action;
import environnement.Etat;
import environnement.MDP;
import environnement.gridworld.ActionGridworld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


/**
 * Cet agent met a jour sa fonction de valeur avec value iteration
 * et choisit ses actions selon la politique calculee.
 *
 * @author laetitiamatignon
 */
public class ValueIterationAgent extends PlanningValueAgent {

    private double gamma;

    private double[] values;

    /**
     * @param gamma
     * @param mdp
     */
    public ValueIterationAgent(double gamma, MDP mdp) {
        super(mdp);
        this.gamma = gamma;
        this.values = new double[mdp.getNbEtats()];
        for(double d : this.values) {
            d = 0;
        }
    }


    public ValueIterationAgent(MDP mdp) {
        this(0.9, mdp);
    }

    /**
     * Mise a jour de V: effectue UNE iteration de value iteration
     */
    @Override
    public void updateV() {
        this.delta = 0.0;
        double[] clValues = new double[mdp.getNbEtats()];
        List<Etat> etats = mdp.getEtatsAccessibles();
        for(Etat cEtat : etats) {
            List<Action> actions = mdp.getActionsPossibles(cEtat);
            ArrayList<Double> max = new ArrayList<Double>();
            for(Action cAction : actions) {
                try {
                    HashMap<Etat, Double> hash = (HashMap<Etat, Double>) mdp.getEtatTransitionProba(cEtat, cAction);
                    double sum = 0;
                    for(Etat dEtat : hash.keySet()) {
                        sum += hash.get(dEtat) * (mdp.getRecompense(cEtat, cAction, dEtat) + gamma * values[dEtat.indice()]);
                    }
                    max.add(sum);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            clValues[cEtat.indice()] =  max.stream().mapToDouble(m -> m.doubleValue()).max().getAsDouble();
        }
        this.values = clValues;
        this.notifyObs();
    }


    /**
     * renvoi l'action donnee par la politique
     */
    @Override
    public Action getAction(Etat e) {
        List<Action> actions = getPolitique(e);
        if(actions.size() == 1) {
            return actions.get(0);
        } else if(actions.size() > 0) {
            Random rdm = new Random(System.currentTimeMillis());
            return actions.get(rdm.nextInt(actions.size()));
        } else {
            return ActionGridworld.NONE;
        }
    }

    @Override
    public double getValeur(Etat _e) {
        return values[_e.indice()];
    }

    /**
     * renvoi action(s) de plus forte(s) valeur(s) dans etat (plusieurs actions sont renvoyees si valeurs identiques, liste vide si aucune action n'est possible)
     */
    @Override
    public List<Action> getPolitique(Etat _e) {
        List<Action> l = new ArrayList<Action>();
        List<Action> actions = mdp.getActionsPossibles(_e);
        double maxvalue=0;
        for(Action cAction : actions) {
            try {
                HashMap<Etat, Double> hash = (HashMap<Etat, Double>) mdp.getEtatTransitionProba(_e, cAction);
                double sum = 0;
                for(Etat dEtat : hash.keySet()) {
                    sum += hash.get(dEtat) * (mdp.getRecompense(_e, cAction, dEtat) + gamma * values[dEtat.indice()]);
                }
                if(sum>maxvalue){
                    maxvalue=sum;
                    l.clear();
                    l.add(cAction);
                }else if(sum==maxvalue){
                    l.add(cAction);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return l;

    }

    @Override
    public void reset() {
        super.reset();
        this.gamma = 0;
        this.values = new double[mdp.getNbEtats()];
        this.notifyObs();
    }


    @Override
    public void setGamma(double arg0) {
        this.gamma = arg0;
    }


}
