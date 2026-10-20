create table usuario (
  id bigserial primary key,
  username varchar(255) unique not null,
  password varchar(255) not null
);

alter table tarjeta add column usuario_id bigint;
alter table tarjeta add constraint fk_tarjeta_usuario foreign key (usuario_id) references usuario (id);
