CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE clients (
    userId serial not null PRIMARY KEY,
    username varchar(50) not null,
    password varchar(50) not null ,
    CONSTRAINT unique_username UNIQUE(username)
);

CREATE OR REPLACE FUNCTION random_bank_acc(userid varchar, length integer) returns text as
$$
declare
    chars text[] := '{0,1,2,3,4,5,6,7,8,9}';
    result text := '';
    i integer := 0;
begin
    if length < 0 then
        raise exception 'Given length cannot be less than 0';
    end if;
    for i in 1..length loop
            if i % 5 <> 0 then
                result := result || chars[1+random()*(array_length(chars, 1)-1)];
            else
                result := result || ' ';
            end if;
        end loop;
    return TRIM(result) || ' ' || $1;
end;
$$ language plpgsql;

CREATE TABLE accounts (
    userId serial not null references clients(userid),
    accId varchar(50) unique not null default random_bank_acc('0', length := 20),
    PRIMARY KEY (accId),
    balance numeric default 0.0
)

