SELECT * FROM Boats WHERE Boats.D > 174;
SELECT S.B FROM Sailors S WHERE S.A > 188;
SELECT S.B FROM Sailors S WHERE S.A > 155 AND S.B > 55 AND S.C <= 32;
SELECT Boats.E FROM Boats WHERE Boats.D <= 175 AND Boats.E > 55 AND Boats.F <= 33;
SELECT R.G FROM Reserves R WHERE R.H > 153 AND R.H < 175 AND R.H < 188;
SELECT * FROM Reserves WHERE Reserves.G != 135 AND Reserves.H = 106;
SELECT * FROM Sailors S1, Sailors S2 WHERE S1.A < S2.A AND S2.A > 46 AND S1.B = S2.B AND S1.C > 155;
SELECT * FROM Sailors, Boats WHERE Sailors.A > 173 AND Sailors.A <= 175 AND Boats.D >=55 AND Boats.D < 111 AND Boats.E > 177;