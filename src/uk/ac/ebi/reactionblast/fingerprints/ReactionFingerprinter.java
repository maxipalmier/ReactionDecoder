/*
 * Copyright (C) 2013 Syed Asad Rahman <asad at ebi.ac.uk>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package uk.ac.ebi.reactionblast.fingerprints;

import java.io.Serializable;
import java.util.BitSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openscience.cdk.Reaction;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.smsd.helper.MoleculeInitializer;
import uk.ac.ebi.reactionblast.fingerprints.interfaces.IPatternFingerprinter;
import uk.ac.ebi.reactionblast.tools.ExtAtomContainerManipulator;

/**
 * @contact Syed Asad Rahman, EMBL-EBI, Cambridge, UK.
 * @author Syed Asad Rahman <asad @ ebi.ac.uk>
 */
public class ReactionFingerprinter implements Serializable {

    private static final long serialVersionUID = 7867867834118778L;
    private final IPatternFingerprinter reactionFingerprint;

    public ReactionFingerprinter(IReaction reaction) throws CDKException {
        IReaction r = expandReactionAndRemoveHydrogens(reaction);
        IPatternFingerprinter fpr = null;
        try {
            fpr = getSumOfFingerprints(r.getReactants());
        } catch (Exception ex) {
            System.err.println("ERROR: while get SumOfFingerprints for Reactants");
        }
        IPatternFingerprinter fpp = null;
        try {
            fpp = getSumOfFingerprints(r.getProducts());
        } catch (Exception ex) {
            System.err.println("ERROR: while get SumOfFingerprints for Products");
        }
        this.reactionFingerprint = summationPatterns(fpr, fpp);
        reactionFingerprint.setFingerprintID(r.getID());
    }

    /**
     *
     * @return
     */
    public synchronized IPatternFingerprinter getReactionStruturalFingerprint() {
        return this.reactionFingerprint;
    }

    /**
     *
     * @param molSet
     * @throws CDKException
     */
    private static IPatternFingerprinter getSumOfFingerprints(IAtomContainerSet molSet) throws CDKException, Exception {
        FingerprintGenerator molFingerprint = new FingerprintGenerator();
        IPatternFingerprinter fp = new PatternFingerprinter(FingerprintGenerator.getFingerprinterSize());
        for (IAtomContainer mol : molSet.atomContainers()) {
            BitSet booleanArray = molFingerprint.getFingerprint(mol);
            for (int i = 0; i < booleanArray.size(); i++) {
                if (booleanArray.get(i)) {
                    fp.add(new Feature(String.valueOf(i), 1.0));
                }
            }
        }
        return fp;
    }

    /**
     *
     * @param bondFeatures1
     * @param bondFeatures2
     * @return
     * @throws CDKException
     */
    private static IPatternFingerprinter summationPatterns(IPatternFingerprinter pattern1, IPatternFingerprinter pattern2) throws CDKException {

        PatternFingerprinter patternFingerprinter = null;
        if (pattern1 != null && pattern2 != null
                && pattern1.getFingerprintSize()
                == pattern2.getFingerprintSize()) {
            patternFingerprinter = new PatternFingerprinter(pattern1.getFingerprintSize());

            patternFingerprinter.add(pattern1);
            patternFingerprinter.add(pattern2);
        } else {
            throw new CDKException("Index < 0: ");
        }
        return patternFingerprinter;
    }

    /*
     * @param reaction
     * @return
     */
    public static IReaction expandReactionAndRemoveHydrogens(IReaction reaction) {
        IReaction r = new Reaction();
        /*
         * imp. to set reactin ID
         */
        String rid = reaction.getID() == null ? Long.toHexString(System.currentTimeMillis()).toUpperCase() : reaction.getID();
        for (IAtomContainer ac : reaction.getReactants().atomContainers()) {
            IAtomContainer ac1 = ac.getBuilder().newInstance(IAtomContainer.class, ac);
            String id = ac.getID() == null ? Long.toHexString(System.currentTimeMillis()).toUpperCase() : ac.getID();
            Double reactantCoefficient = reaction.getReactantCoefficient(ac);
            try {
                try {
                    ac1 = ExtAtomContainerManipulator.removeHydrogensExceptSingleAndPreserveAtomID(ac1);
                } catch (CloneNotSupportedException ex) {
                    Logger.getLogger(ReactionFingerprinter.class.getName()).log(Level.SEVERE, null, ex);
                }
                MoleculeInitializer.initializeMolecule(ac1);
            } catch (CDKException ex) {
                System.err.println("ERROR: while configuring the reaction");
            }
            ac1.setID(id);
            for (int i = 0; i < reactantCoefficient; i++) {
                r.addReactant(ac1, 1.0);
            }
        }
        for (IAtomContainer ac : reaction.getProducts().atomContainers()) {
            IAtomContainer ac1 = ac.getBuilder().newInstance(IAtomContainer.class, ac);
            String id = ac.getID() == null ? Long.toHexString(System.currentTimeMillis()).toUpperCase() : ac.getID();
            Double productCoefficient = reaction.getProductCoefficient(ac);

            try {
                try {
                    ac1 = ExtAtomContainerManipulator.removeHydrogensExceptSingleAndPreserveAtomID(ac1);
                } catch (CloneNotSupportedException ex) {
                    Logger.getLogger(ReactionFingerprinter.class.getName()).log(Level.SEVERE, null, ex);
                }
                MoleculeInitializer.initializeMolecule(ac1);
            } catch (CDKException ex) {
                System.err.println("ERROR: while configuring the reaction");
            }
            ac1.setID(id);
            for (int i = 0; i < productCoefficient; i++) {
                r.addProduct(ac1, 1.0);
            }
        }
        r.setID(rid);
        return r;
    }
}
