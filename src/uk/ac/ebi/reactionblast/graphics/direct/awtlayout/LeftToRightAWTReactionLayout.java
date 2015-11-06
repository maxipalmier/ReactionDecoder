/*
 * Copyright (C) 2007-2015 Syed Asad Rahman <asad @ ebi.ac.uk>.
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

package uk.ac.ebi.reactionblast.graphics.direct.awtlayout;

import java.awt.Graphics2D;

import javax.vecmath.Vector2d;

import org.openscience.cdk.interfaces.IReaction;
import uk.ac.ebi.reactionblast.graphics.direct.Axis;
import uk.ac.ebi.reactionblast.graphics.direct.Params;
import uk.ac.ebi.reactionblast.graphics.direct.layout.BoundsTree;

public class LeftToRightAWTReactionLayout extends AbstractAWTReactionLayout {
    
    public LeftToRightAWTReactionLayout() {
        this(new Params());
    }

    public LeftToRightAWTReactionLayout(Params params) {
        super.params = params;
        super.reactantLayout = new LinearAtomContainerSetLayout(new Vector2d(1, 0));
        super.productLayout = new LinearAtomContainerSetLayout(new Vector2d(1, 0));
        super.arrowAxis = Axis.X;
    }

    @Override
    public BoundsTree layout(IReaction reaction, Graphics2D graphics) {
        String rxnID = reaction.getID();
        
        reactantBoundsTree = reactantLayout.layout(reaction.getReactants(), graphics);
        productBoundsTree = productLayout.layout(reaction.getProducts(), graphics);
        
        int borderX = params.borderX;
        int borderY = params.borderY;
        int arrowGap = params.arrowGap;
        int arrowLength = params.arrowLength;
        
        double rbW = reactantBoundsTree.getWidth();
        double rbH = reactantBoundsTree.getHeight();
        double pbH = productBoundsTree.getHeight();
        double maxH = Math.max(rbH, pbH);
        
        double dx = borderX;
        double dy = borderY + (maxH / 2);
        
        shiftMoleculeSet(reaction.getReactants(), reactantBoundsTree, dx, dy);
        dx = borderX + rbW + arrowLength + (2 * arrowGap);
        shiftMoleculeSet(reaction.getProducts(), productBoundsTree, dx, dy);
        
        boundsTree = new BoundsTree(rxnID, productBoundsTree, reactantBoundsTree);
        double arrowCenterX = borderX + rbW + arrowGap + (arrowLength / 2);
//        System.out.println("setting arrow pos to " + arrowCenterX);
        arrowPos = arrowCenterX;
        
        return boundsTree;
    }
    
    public Vector2d getAxis() {
        return new Vector2d(1, 0);
    }

    @Override
    public BoundsTree layout(IReaction obj, String rootLabel, Graphics2D g) {
        // TODO Auto-generated method stub
        return null;
    }
}
