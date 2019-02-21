
library(readxl)
library(data.table)
library(tidyverse)

data_RSET <- fread("../AME_AMS_dataDGEC/Construction neuve/2017.09.29 - OPE - Export DHUP.csv", sep=";",header = T, skip=2, dec=",")

summary(data_RSET)
names(data_RSET)
  
data_RSET_usage_energie = melt(data_RSET,id.vars = c("id_batiment", "zone_climatique","usage_principal", "cep_projet"), 
                               measure.vars =  c("o_cep_ch_gaz","o_cep_fr_gaz","o_cep_ecs_gaz", 
                                                 "o_cep_ch_fioul","o_cep_fr_fioul","o_cep_ecs_fioul", 
                                                 "o_cep_ch_bois","o_cep_fr_bois","o_cep_ecs_bois",
                                                 "o_cep_ch_reseau_chaleur","o_cep_fr_reseau_chaleur","o_cep_ecs_reseau_chaleur", 
                                                 "o_cep_ch_elec","o_cep_fr_elec","o_cep_ecs_elec","o_cep_ecl_elec", 
                                                 "o_cep_auxv_elec", "o_cep_auxs_elec")
                               )

#### recodage usage
data_RSET_usage_energie[str_detect( variable,"_ch_"),usage := "Chauffage"]
data_RSET_usage_energie[str_detect( variable,"_ecs_"),usage := "ECS"]
data_RSET_usage_energie[str_detect( variable,"_auxv_"),usage := "AuxVent"]
data_RSET_usage_energie[str_detect( variable,"_auxs_"),usage := "AuxS"]
data_RSET_usage_energie[str_detect( variable,"_fr_"),usage := "Froid"]
data_RSET_usage_energie[str_detect( variable,"_ecl_"),usage := "Eclairage"]

#### recodage énergie
data_RSET_usage_energie[str_detect( variable,"gaz"),usage := "Gaz"]
data_RSET_usage_energie[str_detect( variable,"elec"),usage := "Electricité"]
data_RSET_usage_energie[str_detect( variable,"fioul"),usage := "Fioul"]
data_RSET_usage_energie[str_detect( variable,"bois"),usage := "Bois"]
data_RSET_usage_energie[str_detect( variable,"reseau_chaleur"),usage := "Urbain"]


summary(data_RSET_usage_energie)


#### verif cep projet


data_RSET_usage_energie[,cep_projet_recalc := sum(value), by=c("id_batiment", "zone_climatique","usage_principal", "cep_projet")]

data_RSET_usage_energie[cep_projet != cep_projet_recalc &  cep_projet - cep_projet_recalc > 10& id_batiment==25889, ]
