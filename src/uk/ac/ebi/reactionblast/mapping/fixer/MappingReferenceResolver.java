/*
 * Copyright (C) 2003-2015 Syed Asad Rahman <asad @ ebi.ac.uk>.
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
package uk.ac.ebi.reactionblast.mapping.fixer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMapping;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.interfaces.IReaction;

/**
 * Simple class to ensure that the atoms referred to in an atom-atom mapping are the same object reference as in the
 * atom containers of the reactants and products.
 *
 * @author maclean
 *
 */
public class MappingReferenceResolver {

    /**
     * Do an in-place resolve of the mappings.
     *
     * @param reaction
     */
    public static void resolveReferences(IReaction reaction) {
//        printIDs(reaction);

        // make lookup tables for the reactant and product atoms
        Map<String, IAtom> atomIDLookup = new HashMap<>();
        IAtomContainerSet reactants = reaction.getReactants();
        IAtomContainerSet products = reaction.getProducts();
        fillMap(reactants, atomIDLookup, "R");
        fillMap(products, atomIDLookup, "P");

        // convert the atom references in the mappings to atom container refs
        List<IMapping> resolvedMappings = new ArrayList<>();
        IChemObjectBuilder builder = reaction.getBuilder();
        for (IMapping mapping : reaction.mappings()) {

            // we assume that mappings are always R->P!
            IAtom a0 = (IAtom) mapping.getChemObject(0);
            IAtom a1 = (IAtom) mapping.getChemObject(1);
            IAtom rAtom = atomIDLookup.get("R" + a0.getID());
            IAtom pAtom = atomIDLookup.get("P" + a1.getID());

            // create the new mapping out of these atom refs
            resolvedMappings.add(
                    builder.newInstance(IMapping.class, rAtom, pAtom));
        }
//        System.out.println("now has " + reaction.getMappingCount() + " mappings");
        // clear and replace the mappings
        int count = reaction.getMappingCount();
        for (int i = count; i > 0; i--) {
            reaction.removeMapping(i);
        }
//        System.out.println("now has " + reaction.getMappingCount() + " mappings");
        int numberOfMappingsAdded = 0;
        for (IMapping mapping : resolvedMappings) {
            reaction.addMapping(mapping);
            numberOfMappingsAdded++;
        }
//        System.out.println(numberOfMappingsAdded + " mappings added");
//        printIDs(reaction);
    }

    /**
     * Fill a map of string ids to atoms using the atom ID and a unique label for this moleculeSet.
     *
     * @param moleculeSet
     * @param map
     * @param label
     */
    private static void fillMap(
            IAtomContainerSet moleculeSet, Map<String, IAtom> map, String label) {
        for (IAtomContainer atomContainer : moleculeSet.atomContainers()) {
            for (IAtom atom : atomContainer.atoms()) {
                String id = label + atom.getID();
                map.put(id, atom);
            }
        }
    }

    private static void printIDs(IReaction reaction) {
        IAtomContainerSet reactants = reaction.getReactants();
        IAtomContainerSet products = reaction.getProducts();
        for (IAtomContainer reactant : reactants.atomContainers()) {
            System.out.print("[");
            for (int i = 0; i < reactant.getAtomCount(); i++) {
                IAtom atom = reactant.getAtom(i);
                System.out.print(atom.getSymbol() + i + "." + atom.getID() + ",");
            }
            System.out.println("]");
        }
        for (IAtomContainer product : products.atomContainers()) {
            System.out.print("[");
            for (int i = 0; i < product.getAtomCount(); i++) {
                IAtom atom = product.getAtom(i);
                System.out.print(atom.getSymbol() + i + "." + atom.getID() + ",");
            }
            System.out.println("]");
        }
        System.out.print("{");
        for (IMapping mapping : reaction.mappings()) {
            IAtom a0 = (IAtom) mapping.getChemObject(0);
            IAtom a1 = (IAtom) mapping.getChemObject(1);
            System.out.print(String.format("%s-%s, ", a0.getID(), a1.getID()));
        }
        System.out.println("}");
    }
}
