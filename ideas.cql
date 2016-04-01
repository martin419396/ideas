use demo;

drop table if exists ideas;

create table ideas (
  received timeuuid,
  received_date text,
  author text,
  content text,
  primary key (received_date, received)
) with clustering order by (received desc);

insert into ideas (received, received_date, author, content)
values (now(), '2016-03-29', 'David', 'Let''s dance');

insert into ideas (received, received_date, author, content)
values (now(), '2016-03-29', 'René', 'I think, therefore I am');

insert into ideas (received, received_date, author, content)
values (now(), '2016-03-29', 'Ben', 'Hey Why Don''t We Play: Mack the Knife Real Slow & In B Minor');
