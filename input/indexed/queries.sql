SELECT * FROM Sailors;
SELECT Sailors.A FROM Sailors;
SELECT Boats.F, Boats.D FROM Boats;
SELECT * FROM Sailors WHERE Sailors.B >= Sailors.C;
SELECT Sailors.A FROM Sailors WHERE Sailors.B >= Sailors.C
SELECT Sailors.A FROM Sailors WHERE Sailors.B >= Sailors.C AND Sailors.B < Sailors.C;
SELECT DISTINCT * FROM Sailors;
SELECT * FROM Sailors S1, Sailors S2 WHERE S1.A < S2.A;
SELECT B.F, B.D FROM Boats B ORDER BY B.D;