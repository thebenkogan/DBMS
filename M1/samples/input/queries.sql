SELECT * FROM Sailors;
SELECT Sailors.A FROM Sailors;
SELECT S.A FROM Sailors S;
SELECT * FROM Sailors S WHERE S.A < 3;
SELECT * FROM Sailors, Reserves WHERE Sailors.A = Reserves.G;
SELECT * FROM Sailors S1, Sailors S2 WHERE S1.A < S2.A;
SELECT * FROM Sailors ORDER BY Sailors.B;
SELECT * FROM Boats B ORDER BY B.D;
SELECT DISTINCT R.G FROM Reserves R;
SELECT * FROM Boats, Sailors WHERE Boats.E = Sailors.A ORDER BY Boats.D;
SELECT DISTINCT R.G, S.A FROM Reserves R, Sailors S WHERE R.G < 3;
SELECT DISTINCT R.G, S.A FROM Reserves R, Sailors S WHERE R.G < 3 ORDER BY S.A;
SELECT Sailors.B, Sailors.C FROM Sailors ORDER BY Sailors.C
SELECT * FROM Boats, Sailors
SELECT R.G FROM Reserves R WHERE R.G > 3;
SELECT DISTINCT S.A, B.D, R.H FROM Sailors S, Boats B, Reserves R ORDER BY B.D, R.H; 
SELECT * FROM Sailors, Boats WHERE Boats.F = Sailors.A AND Sailors.C > Boats.D ORDER BY Boats.D;
SELECT Boats.D, Boats.F FROM Boats ORDER BY Boats.F;
SELECT DISTINCT S.B, S.C FROM Sailors S WHERE S.C > 100;
SELECT R.G FROM Reserves R WHERE R.G > 2;
SELECT * FROM Reserves R WHERE R.H > 102 ORDER BY R.H;
SELECT DISTINCT * FROM Sailors, Reserves, Boats ORDER BY Sailors.C, Reserves.G, Boats.E;
SELECT R.G, S.B, B.E FROM Reserves R, Sailors S, Boats B WHERE R.G > 2 AND B.E > 2 ORDER BY S.B;
SELECT DISTINCT Sailors.A, Boats.E FROM Sailors, Boats WHERE Sailors.A > Boats.E ORDER BY Boats.E;
SELECT S.C, B.F FROM Sailors S, Boats B WHERE S.C > B.D ORDER BY B.F;
SELECT S.C, B.F FROM Sailors S, Boats B WHERE S.C < B.D ORDER BY S.C, B.F;
SELECT * FROM Boats ORDER BY Boats.F, Boats.E, Boats.D;
SELECT * FROM Reserves ORDER BY Reserves.H;
SELECT * FROM Boats WHERE Boats.E = Boats.F;
SELECT * FROM Sailors, Boats WHERE Sailors.A = Boats.E AND Sailors.C < Boats.D ORDER BY Boats.E;