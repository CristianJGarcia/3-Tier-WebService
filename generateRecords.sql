DROP PROCEDURE IF EXISTS generateRecords;
DELIMITER //
CREATE PROCEDURE generateRecords (IN numToCreate int)
BEGIN
    declare f_name varchar(100) default '';
    declare l_name varchar(100) default '';
    declare DateOfBirth date;
    declare i INT default 0;

    create_loop : loop
        if numToCreate < 1 THEN
            LEAVE create_loop;
        end if;

        SELECT FLOOR(RAND() * (50)) + 1 into i;
        SELECT first_name into f_name from firstnames where id = i;

        SELECT FLOOR(RAND() * (50)) + 1 into i;
        SELECT last_name into l_name from lastnames where id = i;

        SELECT FLOOR(RAND() * (25550)) into i;
        SELECT DATE_ADD("1950-01-01", interval i DAY) into DateOfBirth;

        INSERT INTO People (firstname, lastname, dateOfBirth) VALUES (f_name, l_name, DateOfBirth);
        INSERT INTO Audit (change_msg, changed_by, person_id) VALUES ("added", 1, LAST_INSERT_ID());

        Set numToCreate = numToCreate - 1;

    end loop create_loop;
end //
delimiter ;
