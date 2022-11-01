create or replace function type_of_base_user(_id bigint, out result integer) as $$
	begin
	   select case 
	   		when a10._id is not null then 10
	   		else 12 end
	   from _base_user a12
	   left join _anonymous_user a10 on a10._id = a12._id
	   where a12._id = $1
	   into result;
	end
$$ language plpgsql;

