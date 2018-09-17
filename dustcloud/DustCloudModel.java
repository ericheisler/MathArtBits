import java.util.*;

/**
 * This is a 3D cellular automaton for non-interacting, semi mean field dust clouds
 *
 * Model Component
 *
 */
public class DustCloudModel{
	
	// we have an NxMxL lattice
	// N=x, M=y, L=z
	public int N, M, L;
	// it will run for T steps
	public int maxT, currentT, Tinterval;
	
	// this data is stored for each lattice point
	// there are 8 directions. Store directions in the particle count
	public int[][][][] partcount;
	public int[][][][] tempcount;
	
	// store the nearest neighbor indices for convenience
	public int[][][][] NN;

	// this is the particle data
	public int totalCount;
	public int totalmoving;
	public int countCheck;
	public int[] netFlow;
	// no individual data yet.... yet!

	// these are parameters
	public double stopping;
	public double collision;

	// these are for the maths
	int boundaryType;
	int minval;
	int zeros;
	int[] zerodir;

    /**
     * The constructor
     */
    public DustCloudModel() {
		
		// initialize everything
        N = 300;
		M = 80;
		L = 1;
		maxT = 0;
		currentT = 0;
		Tinterval = 20;
		
		partcount = new int[N][M][L][9];
		tempcount = new int[N][M][L][9];
		NN = new int[N][M][L][16];
		
		setNN2D();
		
		totalCount = 100000;
		countCheck = 100000;
		
		stopping = 0.0;
		collision = 0.0;
		zerodir = new int[]{1,1,1,1,1,1};
		
		boundaryType = 1;
    }
	
	// resets everything (erases particles)
	public void reset(){
		currentT = 0;
		maxT = 0;
		for(int i=0; i<N; i++){
			for(int j=0; j<M; j++){
				for(int k=0; k<L; k++){
					for(int l=0; l<9; l++){
						partcount[i][j][k][l] = 0;
					}
				}
			}
		}
	}

	// sets the values ints = {N, M, L, totalCount, Tinterval} , dubs = {stopping, collision}
	public void setParams(int[] ints, double[] dubs){
		
		if(N != ints[0] || M != ints[1] || L != ints[2]){
			N = ints[0];
			M = ints[1];
			L = ints[2];
			partcount = new int[N][M][L][9];
			tempcount = new int[N][M][L][9];
			NN = new int[N][M][L][16];
			setNN2D();
		}
		if(totalCount != ints[3]){
			totalCount = ints[3];
			countCheck = ints[3];
		}
		
		Tinterval = ints[4];
		stopping = dubs[0];
		collision = dubs[1];
	}
	
	// sets a random initial condition
	public void randomize(){
		// distribute the particles uniformly
		// start by reseting everything
		reset();
		
		Random rand = new Random();
		
		int placex = 0;
		int placey = 0;
		int placez = 0;
		double direct = 0.0;
		for(int i=0; i<totalCount; i++){
			placex = (int)(rand.nextDouble()*N);
			placey = (int)(rand.nextDouble()*M);
			placez = (int)(rand.nextDouble()*L);
			
			// modify direction probabilities to account for nonorthogonality
			direct = rand.nextDouble();
			if(L==1){
				if(direct < 0.134){
					partcount[placex][placey][placez][0]++;
				}else if(direct < 0.268){
					partcount[placex][placey][placez][1]++;
				}else if(direct < 0.4509){
					partcount[placex][placey][placez][2]++;
				}else if(direct < 0.6338){
					partcount[placex][placey][placez][3]++;
				}else if(direct < 0.8167){
					partcount[placex][placey][placez][4]++;
				}else{
					partcount[placex][placey][placez][5]++;
				}
			}else{
				partcount[placex][placey][placez][0]++;
			}
		}
		
		countCheck = 0;
		for(int i=0; i<N; i++){
			for(int j=0; j<M; j++){
				for(int k=0; k<L; k++){
					for(int l=0; l<9; l++){
						countCheck += partcount[i][j][k][l];
					}
				}
			}
		}
	}
	
	// sets up the nearest neighbor indices (periodic)
	public void setNN2D(){
		for(int i=0; i<N; i++){
			for(int j=0; j<M; j++){
				if(i%2==0){
					NN[i][j][0][0] = i+2;
					NN[i][j][0][1] = i+1;
					NN[i][j][0][2] = i-1;
					NN[i][j][0][3] = i-2;
					NN[i][j][0][4] = i-1;
					NN[i][j][0][5] = i+1;
					NN[i][j][0][6] = j;
					NN[i][j][0][7] = j;
					NN[i][j][0][8] = j;
					NN[i][j][0][9] = j;
					NN[i][j][0][10] = j-1;
					NN[i][j][0][11] = j-1;
				}else if(i%2>0){
					NN[i][j][0][0] = i+2;
					NN[i][j][0][1] = i+1;
					NN[i][j][0][2] = i-1;
					NN[i][j][0][3] = i-2;
					NN[i][j][0][4] = i-1;
					NN[i][j][0][5] = i+1;
					NN[i][j][0][6] = j;
					NN[i][j][0][7] = j+1;
					NN[i][j][0][8] = j+1;
					NN[i][j][0][9] = j;
					NN[i][j][0][10] = j;
					NN[i][j][0][11] = j;
				}
				// modify for boundary conditions
				for(int l=0; l<6; l++){
					if(NN[i][j][0][l] > N-1){ NN[i][j][0][l] -= N; }
					if(NN[i][j][0][l+6] > M-1){ NN[i][j][0][l+6] -= M; }
					if(NN[i][j][0][l] < 0){ NN[i][j][0][l] += N; }
					if(NN[i][j][0][l+6] < 0){ NN[i][j][0][l+6] += M; }
				}
				/*
				if(boundaryType == 0){
					for(int l=0; l<6; l++){
						if(NN[i][j][0][l] > N-1){ NN[i][j][0][l] -= N; }
						if(NN[i][j][0][l+6] > M-1){ NN[i][j][0][l+6] -= M; }
						if(NN[i][j][0][l] < 0){ NN[i][j][0][l] += N; }
						if(NN[i][j][0][l+6] < 0){ NN[i][j][0][l+6] += M; }
					}
				}else if(boundaryType == 1){
					for(int l=0; l<6; l++){
						if(NN[i][j][0][l] > N-1){ NN[i][j][0][l] -= N; }
						if(NN[i][j][0][l] < 0){ NN[i][j][0][l] += N; }
						if(NN[i][j][0][l+6] > M-1){ 
							NN[i][j][0][l+6] = -1;
						}
						if(NN[i][j][0][l+6] < 0){ 
							NN[i][j][0][l+6] = -2; 
						}
					}
				}
				*/
			}
		}
		
		
	}
	
	// sets a specified initial condition
	public void setCondition(){
		
		for(int i=0; i<N; i++){
			for(int j=0; j<M; j++){
				for(int l=1; l<6; l++){
					partcount[i][j][0][0] += partcount[i][j][0][l];
					partcount[i][j][0][l] = 0;
				}
				partcount[i][j][0][0] += partcount[i][j][0][8];
				partcount[i][j][0][8] = 0;
			}
		}
	}
	
	// adds a cloud moving in one direction
	public void setCloud2D(int bodiespersite, int xpos, int ypos, int radius, int direction){
		int left, right, top, bottom;
		// I only center at even xpos
		xpos -= xpos%2;
		
		// determine the edges of the cloud
		// if the cloud crosses a boundary, it is cut off
		left = xpos-2*radius;
		if(left < 0){ left = 0; }
		right = xpos+2*radius;
		if(right > N-1){ right = N-1; }
		top = (int)(ypos + 1.732*radius);
		if(top > M-1){ top = M-1; }
		bottom = (int)(ypos - 1.732*radius);
		if(bottom < 0){ bottom = 0; }
		
		// add bodies to all points within the radius
		double distance = 0.0;
		for(int i=left; i<right+1; i++){
			for(int j=bottom; j<top+1; j++){
				if(i%2>0){
					distance = Math.sqrt((i-xpos)*(i-xpos)/4 + 0.75*(2*(j-ypos)+1)*(2*(j-ypos)+1));
				}else{
					distance = Math.sqrt((i-xpos)*(i-xpos)/4 + 3.0*(j-ypos)*(j-ypos));
				}
				if(distance < radius){
					partcount[i][j][0][direction] += bodiespersite;
				}
			}
		}
		
	}
	
	////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////
	// Here is the math for 2D
	//
	// First transfer the particles
	// Then collide (modifying the outflow vector then setting the new partcount)
	// The collision procedure is
	//  1. directly pass some fraction of each flow (parameter a) up to a maximum amount
	//  2. directly pass minimum in
	//  3.  in [1,1,1,1,1,0] out [.5,.5,2,.5,.5,1]
	//  4.a in [1,1,1,1,0,0] out [0,1.5,1.5,0,.5,.5]
	//  4.b in [1,1,1,0,1,0] out [1.5,0,1.5,.5,0,.5]
	//  4.c in [1,1,0,1,1,0] out [.5,.5,1,.5,.5,1]
	//  5.a in [1,1,1,0,0,0] out [1,1,1,0,0,0]
	//  5.b in [1,1,0,1,0,0] out [0,1.5,.5,0,.5,.5]
	//  5.b in [1,1,0,0,1,0] out [1.5,0,.5,.5,0,.5]
	//  5.c in [1,0,1,0,1,0] out [1,0,1,0,1,0]
	//  6.a in [1,1,0,0,0,0] out [1,1,0,0,0,0]
	//  6.b in [1,0,1,0,0,0] out [0,0,0,0,1,0] + s1
	//  6.c in [1,0,0,1,0,0] out [0,.5,.5,0,.5,.5]
	//  7   in [m,0,0,0,0,0] out [0,0,m,0,m,0] - sm   m=max(s,1)
	////////////////////////////////////////////////////////////////////////////
	
	public boolean timeStep2D(){
		minval = 0;
		zeros = 0;
		int i, j, l, tmpl, extra; //for efficiency i=x,j=y,k=z,l=direction
		
		// transfer particles among sites
		// then collide them
		for(i=0; i<N; i++){
			for(j=0; j<M; j++){
				// the pre-collided values are stored in tempcount
				tempcount[i][j][0][0] = partcount[NN[i][j][0][3]][NN[i][j][0][9]][0][0];
				tempcount[i][j][0][1] = partcount[NN[i][j][0][4]][NN[i][j][0][10]][0][1];
				tempcount[i][j][0][2] = partcount[NN[i][j][0][5]][NN[i][j][0][11]][0][2];
				tempcount[i][j][0][3] = partcount[NN[i][j][0][0]][NN[i][j][0][6]][0][3];
				tempcount[i][j][0][4] = partcount[NN[i][j][0][1]][NN[i][j][0][7]][0][4];
				tempcount[i][j][0][5] = partcount[NN[i][j][0][2]][NN[i][j][0][8]][0][5];
				/*
				if(boundaryType==0){
					tempcount[i][j][0][0] = partcount[NN[i][j][0][3]][NN[i][j][0][9]][0][0];
					tempcount[i][j][0][1] = partcount[NN[i][j][0][4]][NN[i][j][0][10]][0][1];
					tempcount[i][j][0][2] = partcount[NN[i][j][0][5]][NN[i][j][0][11]][0][2];
					tempcount[i][j][0][3] = partcount[NN[i][j][0][0]][NN[i][j][0][6]][0][3];
					tempcount[i][j][0][4] = partcount[NN[i][j][0][1]][NN[i][j][0][7]][0][4];
					tempcount[i][j][0][5] = partcount[NN[i][j][0][2]][NN[i][j][0][8]][0][5];
				}
				if(boundaryType == 1){
					if(NN[i][j][0][7] <0){
						// upper boundary
						tempcount[i][j][0][0] = 0;
						tempcount[i][j][0][1] = 0;
						tempcount[i][j][0][2] = 0;
						tempcount[i][j][0][3] = 0;
						tempcount[i][j][0][4] = partcount[NN[i][j][0][5]][NN[i][j][0][11]][0][2];
						tempcount[i][j][0][5] = partcount[NN[i][j][0][4]][NN[i][j][0][10]][0][1];
					}else if(NN[i][j][0][10] <0){
						// lower boundary
						tempcount[i][j][0][0] = 0;
						tempcount[i][j][0][1] = partcount[NN[i][j][0][2]][NN[i][j][0][8]][0][5];
						tempcount[i][j][0][2] = partcount[NN[i][j][0][1]][NN[i][j][0][7]][0][4];
						tempcount[i][j][0][3] = 0;
						tempcount[i][j][0][4] = 0;
						tempcount[i][j][0][5] = 0;
					}else{
						tempcount[i][j][0][0] = partcount[NN[i][j][0][3]][NN[i][j][0][9]][0][0];
						tempcount[i][j][0][1] = partcount[NN[i][j][0][4]][NN[i][j][0][10]][0][1];
						tempcount[i][j][0][2] = partcount[NN[i][j][0][5]][NN[i][j][0][11]][0][2];
						tempcount[i][j][0][3] = partcount[NN[i][j][0][0]][NN[i][j][0][6]][0][3];
						tempcount[i][j][0][4] = partcount[NN[i][j][0][1]][NN[i][j][0][7]][0][4];
						tempcount[i][j][0][5] = partcount[NN[i][j][0][2]][NN[i][j][0][8]][0][5];
					}
				}
				*/
			}
		}
		// colliding
		for(i=0; i<N; i++){
			for(j=0; j<M; j++){
				
				minval = Integer.MAX_VALUE;
				zeros = 0;
				
				// if there are more than 10000 stationary particles, treat it as a solid wall
				//if(tempcount[i][j][0][8] > 10000){
					//zeros = 6;
					// slip and non-slip conditions
					
					// freeze any moving particles within the wall (should only happen once)
					
					//break;
				//}
				
				// handle non-periodic boundaries
				if(boundaryType == 1){
					if(j==0){
						if(i%2==0){
							partcount[i][j][0][0] = tempcount[i][j][0][0];
							partcount[i][j][0][1] = tempcount[i][j][0][5]+tempcount[i][j][0][1];
							partcount[i][j][0][2] = tempcount[i][j][0][4]+tempcount[i][j][0][2];
							partcount[i][j][0][3] = tempcount[i][j][0][3];
							partcount[i][j][0][4] = 0;
							partcount[i][j][0][5] = 0;
							
							tempcount[i][j][0][0] = 0;
							tempcount[i][j][0][1] = 0;
							tempcount[i][j][0][2] = 0;
							tempcount[i][j][0][3] = 0;
							tempcount[i][j][0][4] = 0;
							tempcount[i][j][0][5] = 0;
							
							continue;
						}
					}
					if(j==M-1){
						if(i%2==1){
							partcount[i][j][0][0] = tempcount[i][j][0][0];
							partcount[i][j][0][1] = 0;
							partcount[i][j][0][2] = 0;
							partcount[i][j][0][3] = tempcount[i][j][0][3];
							partcount[i][j][0][4] = tempcount[i][j][0][2]+tempcount[i][j][0][4];
							partcount[i][j][0][5] = tempcount[i][j][0][1]+tempcount[i][j][0][5];
							
							tempcount[i][j][0][0] = 0;
							tempcount[i][j][0][1] = 0;
							tempcount[i][j][0][2] = 0;
							tempcount[i][j][0][3] = 0;
							tempcount[i][j][0][4] = 0;
							tempcount[i][j][0][5] = 0;
							
							continue;
						}
					}
				}
				
				// directly pass the fraction 1-collision in each direction (using floor)
				for(l=0; l<6; l++){
					tmpl = (int)((1.0-collision)*tempcount[i][j][0][l]);
					if(tmpl <= 0){ tmpl = 0; }
					partcount[i][j][0][l] = tmpl;
					tempcount[i][j][0][l] -= tmpl;
				}
				
				// perform all head on collisions
				for(l=0; l<3; l++){
					if(tempcount[i][j][0][l] > 0 && tempcount[i][j][0][(l+3)%6] > 0){
						minval = tempcount[i][j][0][l];
						if(tempcount[i][j][0][(l+3)%6] < minval){
							minval = tempcount[i][j][0][(l+3)%6];
						}
						extra = minval%2;
						
						partcount[i][j][0][(l+1)%6] += minval/2;
						partcount[i][j][0][(l+2)%6] += minval/2;
						partcount[i][j][0][(l+4)%6] += minval/2;
						partcount[i][j][0][(l+5)%6] += minval/2;
						// take care of the extras
						if(extra == 1){
							if(currentT%2==0){
								partcount[i][j][0][(l+1)%6] += 1;
								partcount[i][j][0][(l+4)%6] += 1;
							}else{
								partcount[i][j][0][(l+2)%6] += 1;
								partcount[i][j][0][(l+5)%6] += 1;
							}
						}
						
						tempcount[i][j][0][l] -= minval;
						tempcount[i][j][0][(l+3)%6] -= minval;
					}
				}
				// now there are at least 3 zeros and the rest are non opposing
				
				// perform all wide angled collisions
				for(l=0; l<6; l++){
					if(tempcount[i][j][0][l] > 0 && tempcount[i][j][0][(l+2)%6] > 0){
						// in this case let's be careful
						if(tempcount[i][j][0][(l+4)%6] > 0){
							minval = (int)(stopping*tempcount[i][j][0][l]);
							tmpl = l;
							if((int)(stopping*tempcount[i][j][0][(l+2)%6]) < minval){
								minval = (int)(stopping*tempcount[i][j][0][(l+2)%6]);
								tmpl = (l+2)%6;
							}
							if((int)(stopping*tempcount[i][j][0][(l+4)%6]) < minval){
								minval = (int)(stopping*tempcount[i][j][0][(l+4)%6]);
								tmpl = (l+4)%6;
							}
							
							// use up all of the minimum direction
							// the proportions depend on the numbers in the other directions
							extra = minval - (minval*tempcount[i][j][0][(tmpl+2)%6])/tempcount[i][j][0][(tmpl+4)%6];
							
							partcount[i][j][0][(tmpl+4)%6] += minval-extra;
							partcount[i][j][0][8] += minval-extra;
							
							tempcount[i][j][0][tmpl] -= minval-extra;
							tempcount[i][j][0][(tmpl+2)%6] -= minval-extra;
							
							partcount[i][j][0][(tmpl+2)%6] += extra;
							partcount[i][j][0][8] += extra;
							
							tempcount[i][j][0][tmpl] -= extra;
							tempcount[i][j][0][(tmpl+4)%6] -= extra;
							
							// now use up the remaining ones
							if(tempcount[i][j][0][(tmpl+2)%6] > 0 && tempcount[i][j][0][(tmpl+4)%6] > 0){
								minval = (int)(stopping*tempcount[i][j][0][(tmpl+2)%6]);
								if((int)(stopping*tempcount[i][j][0][(tmpl+4)%6]) < minval){
									minval = (int)(stopping*tempcount[i][j][0][(tmpl+4)%6]);
								}
								
								partcount[i][j][0][tmpl] += minval;
								partcount[i][j][0][8] += minval;
								
								tempcount[i][j][0][(tmpl+2)%6] -= minval;
								tempcount[i][j][0][(tmpl+4)%6] -= minval;
							}
							
						}else{
							minval = (int)(stopping*tempcount[i][j][0][l]);
							if((int)(stopping*tempcount[i][j][0][(l+2)%6]) < minval){
								minval = (int)(stopping*tempcount[i][j][0][(l+2)%6]);
							}
							
							partcount[i][j][0][(l+4)%6] += minval;
							partcount[i][j][0][8] += minval;
							
							tempcount[i][j][0][l] -= minval;
							tempcount[i][j][0][(l+2)%6] -= minval;
						}
					}
				}
				
				// if there is anything left, collide it with stationary ones
				// or simply pass it along
				extra = 0;
				for(l=0; l<6; l++){
					extra += tempcount[i][j][0][l];
				}
				if(extra > 0){
					// collide
					if(partcount[i][j][0][8] > 0){
						for(l=0; l<6; l++){
							if(tempcount[i][j][0][l] > 0){
								minval = tempcount[i][j][0][l];
								if((int)(partcount[i][j][0][8]*tempcount[i][j][0][l]*1.0/extra) < minval){
									minval = (int)(partcount[i][j][0][8]*tempcount[i][j][0][l]*1.0/extra);
								}
								
								partcount[i][j][0][(l+1)%6] += minval;
								partcount[i][j][0][(l+5)%6] += minval;
								
								tempcount[i][j][0][l] -= minval;
								partcount[i][j][0][8] -= minval;
							}
						}
					}
					if(partcount[i][j][0][8] > 0){
						// one more sweep to clear them
						for(l=0; l<6; l++){
							tmpl = (l+currentT%6)%6;
							if(tempcount[i][j][0][tmpl] > 0 && partcount[i][j][0][8] > 0){
								minval = tempcount[i][j][0][tmpl];
								if(partcount[i][j][0][8] < minval){
									minval = partcount[i][j][0][8];
								}
								
								partcount[i][j][0][(tmpl+1)%6] += minval;
								partcount[i][j][0][(tmpl+5)%6] += minval;
								
								tempcount[i][j][0][tmpl] -= minval;
								partcount[i][j][0][8] -= minval;
							}
						}
					}
				}
				
				// finally pass everything left
				for(l=0; l<6; l++){
					partcount[i][j][0][l] += tempcount[i][j][0][l];
					
					tempcount[i][j][0][l] = 0;
				}
				
				// at this point all particles should be handled
			}
		}
		
		countCheck = 0;
		totalmoving = 0;
		for(i=0; i<N; i++){
			for(j=0; j<M; j++){
				for(l=0; l<6; l++){
					countCheck += partcount[i][j][0][l];
					totalmoving += partcount[i][j][0][l];
				}
				countCheck += partcount[i][j][0][8];
			}
		}
		
		// don't forget to increment time, silly
		currentT++;
		
		return true;
	}
	
	
	
	////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////
	// Here is the math for 3D
	////////////////////////////////////////////////////////////////////////////
	public boolean timeStep3D(){
		// NOT DONE YET
		
		return false;
	}
	
	public void periodicBoundary3D(){
		// periodic boundary conditions
		
	}
	
}
