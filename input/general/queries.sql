SELECT * FROM Sailors, Reserves WHERE Sailors.A = Reserves.G;
SELECT * FROM Sailors, Reserves, Boats WHERE Sailors.A = Reserves.G AND Reserves.H = Boats.D;
SELECT * FROM Sailors S, Reserves R, Boats B WHERE S.A = R.G AND R.H = B.D ORDER BY S.C;
SELECT DISTINCT * FROM Sailors S, Reserves R, Boats B WHERE S.A = R.G AND R.H = B.D ORDER BY S.C;
