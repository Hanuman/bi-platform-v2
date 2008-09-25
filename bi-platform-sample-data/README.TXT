For mysql5, oracle10g, and postgresql Installs, run the sql create scripts in
the following order:

create_repository_<db>.sql
create_quartz_<db>.sql

If you would like to use Pentaho's default sample users and hsqldb sample data
source, run the following script after running the create scripts:

load_sample_users_<db>.sql