INSERT INTO users (account, password, name, ssn, phone, address, role)
VALUES (
   'admin',
   '$2a$10$kAV1sCUA9SkrbMZ/07dJVu8eXTtyAjwHhb.7pzrrpX07dPzEbOFce',
   '관리자',
   '00000000000',
   '01000000000',
   '서울특별시 종로구',
   'ADMIN'
) ON DUPLICATE KEY UPDATE account = account;
