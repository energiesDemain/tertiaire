source("analyse_param_utilisateurs.R")

parc_total = etiq_ini[,sum(SURFACES)]
#### part des différents occupants

etiq_ini[, sum(SURFACES)/parc_total , by="OCCUPANT"]

#### verif calcul conso 
etiq_ini[, sum(SURFACES)/10^6]
etiq_ini[, sum(CONSOU_TOT*SURFACES)/10^9]
etiq_ini[, sum(CONSO_CHAUFF_EP,na.rm=T)/10^9]

#### part des étiquettes 

etiq_ini[, round(sum(SURFACES)/parc_total,digits= 2), by=ETIQUETTE][order(ETIQUETTE)]


#### batiment_publics 
COD_OCCUPANT
COD_BRANCHE
etiq_ini[COD_BRANCHE == "04" & COD_OCCUPANT == "05", sum(SURFACES)] 
parc_public = etiq_ini[COD_OCCUPANT%in% c("01","02","03","04","06"), ]


parc_public[,sum(SURFACES)]/10^6


#### part des différents occupants

parc_public[, sum(SURFACES)/sum(parc_public$SURFACES) , by="OCCUPANT"]

#### part des différentes branche par occupant
parc_public_occupant = parc_public[, list(Surf_occupant = sum(SURFACES)) , by=c("OCCUPANT")]

parc_public_occupant_branche = parc_public[, list(surf_branche = sum(SURFACES)) , by=c("OCCUPANT","BRANCHE")]

parc_public_occupant_branche = merge(parc_public_occupant_branche,parc_public_occupant ,by="OCCUPANT")
parc_public_occupant_branche[,Part_branche:=surf_branche/Surf_occupant*100]

#### part des etiquettes dans le parc public 

parc_public[, sum(SURFACES)/sum(parc_public$SURFACES) , by="ETIQUETTE"]

#### passoires parc public 

parc_public[ETIQUETTE %in% c("D","E","F","G"),list(SURFACES = sum(SURFACES)/10^6, 
                                                   CONSOTOT = sum(CONSOU_TOT*SURFACES)/10^9,
                                                   CONSO_CHAUFF = sum(CONSO_CHAUFF)/10^9), by=""]


parc_public[ETIQUETTE %in% c("F","G"),list(SURFACES = sum(SURFACES)/10^6, 
                                           CONSOTOT = sum(CONSOU_TOT*SURFACES)/10^9,
                                           CONSO_CHAUFF = sum(CONSO_CHAUFF)/10^9), by=""]


toto =parc_public[, list(BRANCHE, OCCUPANT)]

parc_public_passoire = 
  parc_public[ETIQUETTE %in% c("F","G"),]

parc_public_passoire  = 
  parc_public[ETIQUETTE %in% c("E","F","G"),]


toto2 =parc_public_passoire [, list( OCCUPANT,BRANCHE,SS_BRANCHE)][order(OCCUPANT)]

#### part des différents occupants dans les passoires 

parc_public_passoire[, sum(SURFACES)/sum(parc_public_passoire$SURFACES) , by="OCCUPANT"]

### part des différentes branche par occupant dans les passoires
parc_public_passoire_occupant = parc_public_passoire[, list(Surf_occupant = sum(SURFACES)) , by=c("OCCUPANT")]

parc_public_passoire_occupant_branche = parc_public_passoire[, list(surf_branche = sum(SURFACES)) , by=c("OCCUPANT","BRANCHE")]

parc_public_passoire_occupant_branche = merge(parc_public_passoire_occupant_branche,parc_public_passoire_occupant ,by="OCCUPANT")
parc_public_passoire_occupant_branche[,Part_branche:=surf_branche/Surf_occupant*100]


#### cout ensemble BBC

cout_geste_tmp = cout_geste[,list(COD_PERIODE_SIMPLE,COD_PERIODE_DETAIL,
                                  COD_BAT_TYPE,COD_SS_BRANCHE,COD_BRANCHE,
                                  GESTE, GAIN, COUT,CEE)]


cout_geste_tmp[, ID_merge := paste0(COD_BRANCHE,COD_SS_BRANCHE,COD_BAT_TYPE, COD_PERIODE_DETAIL,
                                    COD_PERIODE_SIMPLE),]

cout_geste_tmp = melt(cout_geste_tmp[,list( ID_merge,GESTE, GAIN, COUT,CEE)], id.vars = c("ID_merge", "GESTE"))


cout_geste_casted = dcast.data.table(cout_geste_tmp,  ID_merge  ~ variable + GESTE )

parc_public_passoire[, ID_merge := paste0(COD_BRANCHE,COD_SS_BRANCHE,COD_BAT_TYPE, COD_PERIODE_DETAIL,
                                 COD_PERIODE_SIMPLE)]

parc_public_passoire  = merge(parc_public_passoire ,cout_geste_casted, by= "ID_merge" ,all.x=T)

#### on impute le coût et le gain moyen pour les segments où il est manquant

parc_public_passoire[is.na(COUT_ENSBBC),COUT_ENSBBC:=
                       parc_public_passoire[,mean(COUT_ENSBBC, na.rm=T)]]
parc_public_passoire[is.na(GAIN_ENSBBC),GAIN_ENSBBC:=
                       parc_public_passoire[,mean(GAIN_ENSBBC, na.rm=T)]]


parc_public_passoire[,COUT_TOT_ENSBBC :=  COUT_ENSBBC*SURFACES ]
parc_public_passoire[,GAIN_TOT_ENSBBC :=  GAIN_ENSBBC*(CONSO_CHAUFF+BESOIN_AUXILIAIRES_CALC+
                                                BESOIN_VENTILATION) ]

parc_public_passoire[,list(COUT_TOT_ENSBBC =  sum(COUT_TOT_ENSBBC)/10^9, 
                  GAIN_TOT_ENSBBC = sum(GAIN_TOT_ENSBBC)/10^9)]


gains_tot = parc_public_passoire[,list(COUT_TOT_ENSBBC =  sum(COUT_ENSBBC*SURFACES)/10^9, 
                  GAIN_TOT_ENSBBC = sum(GAIN_ENSBBC*(CONSO_CHAUFF+BESOIN_AUXILIAIRES_CALC+
                                                       BESOIN_VENTILATION))/10^9)]

gains_paroccupant = parc_public_passoire[,list(COUT_TOT_ENSBBC =  sum(COUT_ENSBBC*SURFACES)/10^9, 
                                       GAIN_TOT_ENSBBC = sum(GAIN_ENSBBC*(CONSO_CHAUFF+BESOIN_AUXILIAIRES_CALC+
                                                                            BESOIN_VENTILATION))/10^9),
                                       by="OCCUPANT"]

gains_parbranche = parc_public_passoire[,list(COUT_TOT_ENSBBC =  sum(COUT_ENSBBC*SURFACES)/10^9, 
                                               GAIN_TOT_ENSBBC = sum(GAIN_ENSBBC*(CONSO_CHAUFF+BESOIN_AUXILIAIRES_CALC+
                                                                                    BESOIN_VENTILATION))/10^9),
                                        by="BRANCHE"]




parc_public_passoire[,mean(COUT_ENSBBC, na.rm=T)]*parc_public_passoire[,sum(SURFACES)]/10^9
parc_public_passoire[,mean(GAIN_ENSBBC)]*parc_public_passoire[, sum(CONSO_CHAUFF,BESOIN_AUXILIAIRES_CALC,
                                                                 BESOIN_VENTILATION)]/10^9


parc_public_passoire[,sum(CONSO_CHAUFF, CONSO_ECS, CONSO_CLIM, 
                 BESOIN_AUXILIAIRES_CALC, BESOIN_VENTILATION, 
                 BESOIN_ECLAIRAGE,BESOIN_BUREAUTIQUE,BESOIN_FROID,
                 BESOIN_PROCESS,BESOIN_AUTRES,BESOIN_CUISSON, na.rm=T)]/10^9

parc_public_passoire[, sum(CONSO_CHAUFF,BESOIN_AUXILIAIRES_CALC,
                                                  BESOIN_VENTILATION)]/10^9

#### gains facture

#### part du parc public par énergie
part_ener = besoins_parc_init[COD_OCCUPANT%in% c("01","02","03","04","06"),sum(SURFACES), by="ENERGIE"]

### prix ener en 2020 0.126832147	0.064132015	0.085555567	0.062280998	0.070402999
### prix ener en 2030 0.13653712	0.088697931	0.140114	0.062906618	0.080109794

### prix moyen energie 
pmoy_ener = (part_ener[ENERGIE == "Electricité",V1]*0.13653712 + 
               part_ener[ENERGIE == "Gaz",V1]*0.088697931 + 
  part_ener[ENERGIE == "Fioul",V1]*0.140114 +  part_ener[ENERGIE == "Urbain",V1]*0.062906618 + 
  part_ener[ENERGIE == "Autres",V1]*0.080109794)/sum(part_ener$V1)

parc_public_passoire[,list(COUT_TOT_ENSBBC =  sum(COUT_ENSBBC*SURFACES)/10^9, 
                  GAIN_TOT_ENSBBC = sum(GAIN_ENSBBC*(CONSO_CHAUFF+BESOIN_AUXILIAIRES_CALC+
                                                       BESOIN_VENTILATION))/10^9)]

gains_tot$GAIN_TOT_ENSBBC*pmoy_ener  

gains_paroccupant[,Gains_factures := GAIN_TOT_ENSBBC*pmoy_ener]  
gains_parbranche[,Gains_factures := GAIN_TOT_ENSBBC*pmoy_ener]  

#### gains CO2
### contenu moyen CO2
CO2_moy = (part_ener[ENERGIE == "Electricité",V1]*180 + 
               part_ener[ENERGIE == "Gaz",V1]*205 + 
               part_ener[ENERGIE == "Fioul",V1]*271 +  part_ener[ENERGIE == "Urbain",V1]*195 + 
               part_ener[ENERGIE == "Autres",V1]*115)/sum(part_ener$V1)

gains_tot$GAIN_TOT_ENSBBC*10^9*CO2_moy/10^12

gains_paroccupant[,Gains_CO2 := GAIN_TOT_ENSBBC*10^9*CO2_moy/10^12]  
gains_parbranche[,Gains_factures := GAIN_TOT_ENSBBC*10^9*CO2_moy/10^12]  
