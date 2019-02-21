require(data.table)
require(ggplot2)
require(tidyverse)

data_couts_gestes = fread("../docs_completementaires_EN/tertiaire_cgdd_brut_V2.csv", sep = ";", dec=",")
repart_gain = fread("../docs_completementaires_EN/Repart_gain_gestes.csv", sep=";", dec=",")
repart_gain = fread("../docs_completementaires_EN/Repart_gain_gestes_V2.csv", sep=";", dec=",")


data_couts_gestes[SCENARIO == "Etat initial",sum(SURFACE_CHAUFFEE)]
##### nomenclatures par tertiaire ######

COD_BRANCHE = fread("table_param_origine/Nomenclature_csv/COD_BRANCHE.csv", colClasses = "character")
COD_SS_BRANCHE = fread("table_param_origine/Nomenclature_csv/COD_SS_BRANCHE.csv", colClasses = "character")
COD_BAT_TYPE = fread("table_param_origine/Nomenclature_csv/COD_BAT_TYPE.csv", colClasses = "character")
COD_ENERGIE = fread("table_param_origine/Nomenclature_csv/COD_ENERGIE.csv", colClasses = "character")
COD_OCCUPANT = fread("table_param_origine/Nomenclature_csv/COD_OCCUPANT.csv", colClasses = "character")
COD_PERIODE_DETAIL = fread("table_param_origine/Nomenclature_csv/COD_PERIODE_DETAIL.csv", colClasses = "character")
COD_PERIODE_SIMPLE = fread("table_param_origine/Nomenclature_csv/COD_PERIODE_SIMPLE.csv", colClasses = "character")
COD_SYSTEME_CHAUD = fread("table_param_origine/Nomenclature_csv/COD_SYSTEME_CHAUD.csv", colClasses = "character")
COD_SYSTEME_FROID = fread("table_param_origine/Nomenclature_csv/COD_SYSTEME_FROID.csv", colClasses = "character")
COD_USAGE = fread("table_param_origine/Nomenclature_csv/COD_USAGE.csv", colClasses = "character")
COD_ZONE_CLIMAT = fread("table_param_origine/Nomenclature_csv/COD_ZONE_CLIMAT.csv", colClasses = "character")


#### ajout des codes à la base brute sur les coûts

data_couts_gestes = merge(data_couts_gestes, COD_BRANCHE, by="BRANCHE")
data_couts_gestes = merge(data_couts_gestes, COD_SS_BRANCHE, by="SS_BRANCHE")
data_couts_gestes = merge(data_couts_gestes, COD_BAT_TYPE, by="BAT_TYPE")
data_couts_gestes = merge(data_couts_gestes, COD_PERIODE_DETAIL, by="PERIODE_DETAIL")
data_couts_gestes = merge(data_couts_gestes, COD_PERIODE_SIMPLE, by="PERIODE_SIMPLE")

data_couts_gestes[,ID_AGREG := paste0(COD_BRANCHE, COD_SS_BRANCHE, COD_BAT_TYPE,COD_PERIODE_DETAIL, COD_PERIODE_SIMPLE)]


data_couts_gestes[COD_BAT_TYPE == "42" & PERIODE_DETAIL == "Av 1980" & SCENARIO !="Maximal" & SS_BRANCHE == "Administration",][order(SCENARIO)]


## renomme geste

data_couts_gestes[, GESTE := factor(SCENARIO, levels = c("Etat initial","Modeste bati","BBC renovation","Maximal"), 
                                    labels = c("Etat initial", "ENS_MOD","ENS_BBC","ENS_MAX"))]


## aggregation sur toutes les énergies 
data_couts_gestes  = melt(data_couts_gestes, id.vars = c("SCENARIO","GESTE","BRANCHE","SS_BRANCHE","BAT_TYPE",
                                                          "PERIODE_DETAIL", "PERIODE_SIMPLE",
                                                         "COD_BRANCHE","COD_SS_BRANCHE","COD_BAT_TYPE",
                                                         "COD_PERIODE_DETAIL", "COD_PERIODE_SIMPLE","ENERGIE","ID_AGREG"))


data_couts_gestes = dcast.data.table(data_couts_gestes, 
                                     BRANCHE+SS_BRANCHE+BAT_TYPE+PERIODE_DETAIL+PERIODE_SIMPLE+ 
                                     COD_BRANCHE+COD_SS_BRANCHE+COD_BAT_TYPE+COD_PERIODE_DETAIL+COD_PERIODE_SIMPLE+
                                     ID_AGREG+  GESTE~ variable, 
                                     fun.aggregate = sum, value.var = "value") 

data_couts_gestes[COD_BAT_TYPE == "42" & PERIODE_DETAIL == "Av 1980" & GESTE !="ENS_MAX" & 
                    SS_BRANCHE == "Administration",][order(GESTE)]


#### on enlève les segments non chauffés

data_couts_gestes = data_couts_gestes[SURFACE_CHAUFFEE!=0 ]
data_couts_gestes[SURFACE_CHAUFFEE == 0]

#### calcul gain total du geste

data_couts_gestes = merge(data_couts_gestes, 
                          data_couts_gestes[GESTE == "Etat initial",list(BESOIN_INIT = BESOIN),  
                                            by=c("BRANCHE","SS_BRANCHE","BAT_TYPE",
                                                 "PERIODE_DETAIL", "PERIODE_SIMPLE")], 
                          by=c("BRANCHE","SS_BRANCHE","BAT_TYPE",
                               "PERIODE_DETAIL", "PERIODE_SIMPLE"))

data_couts_gestes[, GAIN_TOT := 1-BESOIN/BESOIN_INIT]

summary(data_couts_gestes$GAIN_TOT)

quantile(data_couts_gestes$GAIN_TOT,probs = seq(0,1,0.1))

#### calcul couts moyens par m2 de shon ensemble du geste et par paroi

data_couts_gestes[,COUT_PLAN_MOY_M2 :=  (COUT_PLAN_MIN +  COUT_PLAN_MAX)/2/SURFACE_CHAUFFEE]
data_couts_gestes[,COUT_FEN_MOY_M2 :=  (COUT_FEN_MIN +  COUT_FEN_MAX)/2/SURFACE_CHAUFFEE]
data_couts_gestes[,COUT_MUR_MOY_M2 :=  (COUT_MUR_MIN +  COUT_MUR_MAX)/2/SURFACE_CHAUFFEE]
data_couts_gestes[,COUT_TOIT_MOY_M2 :=  (COUT_TOIT_MIN +  COUT_TOIT_MAX)/2/SURFACE_CHAUFFEE]
data_couts_gestes[,COUT_ENS_MOY_M2 :=  COUT_PLAN_MOY_M2 + COUT_FEN_MOY_M2 + COUT_MUR_MOY_M2 + COUT_TOIT_MOY_M2]
data_couts_gestes[,COUT_FENMUR_MOY_M2 :=  COUT_FEN_MOY_M2 + COUT_MUR_MOY_M2]

#### modification on conserve le coût max seulement
data_couts_gestes[,COUT_PLAN_MOY_M2 :=  COUT_PLAN_MAX/SURFACE_CHAUFFEE]
data_couts_gestes[,COUT_FEN_MOY_M2 := COUT_FEN_MAX/SURFACE_CHAUFFEE]
data_couts_gestes[,COUT_MUR_MOY_M2 :=   COUT_MUR_MAX/SURFACE_CHAUFFEE]
data_couts_gestes[,COUT_TOIT_MOY_M2 :=  COUT_TOIT_MAX/SURFACE_CHAUFFEE]
data_couts_gestes[,COUT_ENS_MOY_M2 :=  COUT_PLAN_MOY_M2 + COUT_FEN_MOY_M2 + COUT_MUR_MOY_M2 + COUT_TOIT_MOY_M2]
data_couts_gestes[,COUT_FENMUR_MOY_M2 :=  COUT_FEN_MOY_M2 + COUT_MUR_MOY_M2]

summary(data_couts_gestes)
data_couts_gestes[is.na(COUT_ENS_MOY_M2)]
quantile(data_couts_gestes$COUT_ENS_MOY_M2,probs = seq(0,1,0.1))

data_couts_gestes_save <- data_couts_gestes
data_couts_gestes_save 

#### verif gain nul etat initial
data_couts_gestes[GESTE == "Etat initial" & GAIN_TOT != 0]

#### gain nul et coûts non nuls
data_couts_gestes[GESTE != "Etat initial" & GAIN_TOT == 0 & COUT_ENS_MOY_M2 !=0] 

#### gain inférieur à zéro et coût nul, on le met à 0 

data_couts_gestes[GAIN_TOT <0]
data_couts_gestes[GAIN_TOT <0 & COUT_ENS_MOY_M2 == 0, GAIN_TOT := 0]

#### coûts nuls et gains non nuls, on met le gain à 0
data_couts_gestes[ COUT_ENS_MOY_M2 == 0 & GAIN_TOT!=0, ]
data_couts_gestes[ COUT_ENS_MOY_M2 == 0, GAIN_TOT := 0]


#### gain très faible 
data_couts_gestes[GAIN_TOT < 0.05& GAIN_TOT !=0]

#### verif si on a des coûts pour tous les segments existant dans le modèle

besoins_chauffage_init = fread("../tertiaire/parametrage modèle/Besoin_chauffage_init.csv", dec = ",", 
                               colClasses =
                                 list("character"=c("ID_segment","ID_AGREG2", "ID_AGREG", "ID_AGREG_RDT_COUT", 
                                                    "ID","ID_BRANCHE","ID_BAT_TYPE","ID_PRODUCTION_CHAUD","ID_ENERGIE")))

besoins_chauffage_init[,ID_AGREG := paste0(substring(ID, 1,6), substring(ID, 9,12))] 
besoins_chauffage_init[,ID_AGREG_calibCINT := paste0(substring(ID, 1,8), substring(ID, 9,10))] 

#####  listes des bâtiments avec aucun geste 
liste_ID_parc = besoins_chauffage_init[,list(ID_AGREG  ,
                                             `SURFACES 2009`,BESOIN_U, ID_BRANCHE, ID_BAT_TYPE )] 


liste_ID_parc[ID_AGREG %in% data_couts_gestes[GESTE!="Etat initial",ID_AGREG] == F]
liste_ID_parc[ID_AGREG %in% data_couts_gestes[GESTE!="Etat initial" & COUT_ENS_MOY_M2 !=0 &  GESTE  != "ENS_MAX"]$ID_AGREG == F]

ID_sans_gestes = liste_ID_parc[ID_AGREG %in% data_couts_gestes[GESTE!="Etat initial" & COUT_ENS_MOY_M2 !=0 &  GESTE  != "ENS_MAX"]$ID_AGREG == F][,unique(ID_AGREG)]
data_couts_gestes[ID_AGREG%in%ID_sans_gestes, BAT_TYPE]

#### surface du parc sans gestes
besoins_chauffage_init[ID_AGREG%in%ID_sans_gestes,sum(`SURFACES 2009 recal`)]/10^6

### comparaison surfaces chauffées
surf_comp = merge(data_couts_gestes[GESTE == "Etat initial",list( ID_AGREG, SURFACE_CHAUFFEE)], besoins_chauffage_init[,list(surface = sum(`SURFACES 2009`), sum(`SURFACES 2009 recal`)),  by="ID_AGREG"], by="ID_AGREG")

surf_comp[surface-SURFACE_CHAUFFEE < -1000]
surf_comp[surface-SURFACE_CHAUFFEE > 1000]
### ### ### ### ### ### ### 
#######  stats par geste 
### ### ### ### ### ### ### 

#### gain et coût d'ensemble moyen par geste
data_couts_gestes[GAIN_TOT !=0 & COUT_ENS_MOY_M2 !=0 ,list(GAIN = mean(GAIN_TOT), COUT_ENS_MOY_M2 = mean( COUT_ENS_MOY_M2)), by=c("GESTE")]
data_couts_gestes[GAIN_TOT !=0 ,list(GAIN = mean(GAIN_TOT), COUT_ENS_MOY_M2 = mean( COUT_ENS_MOY_M2)), by=c("GESTE")]

#### gain et coût d'ensemble moyen par geste et par branche
data_couts_gestes[GAIN_TOT !=0 ,list(GAIN = mean(GAIN_TOT), COUT_ENS_MOY_M2 = mean( COUT_ENS_MOY_M2)), by=c("GESTE", "BRANCHE")][order(BRANCHE)]

ggplot(data_couts_gestes[,mean(COUT_ENS_MOY_M2), by=c("COD_BRANCHE", "GESTE")], aes(COD_BRANCHE, V1))+
  geom_bar(stat="identity") + facet_wrap(~GESTE)

#### gain et coût d'ensemble moyen par geste unitaire

data_couts_gestes[GAIN_TOT !=0 &  COUT_FEN_MOY_M2 !=0 ,list(GAIN = mean(GAIN_TOT), 
                                                            COUT_FEN_MOY_M2 = mean( COUT_FEN_MOY_M2)),
                  by=c("GESTE")]
data_couts_gestes[GAIN_TOT !=0 &  COUT_MUR_MOY_M2 !=0 ,list(GAIN = mean(GAIN_TOT), 
                                                            COUT_MUR_MOY_M2 = mean( COUT_MUR_MOY_M2)),
                  by=c("GESTE")]
data_couts_gestes[GAIN_TOT !=0 &  COUT_PLAN_MOY_M2 !=0 ,list(GAIN = mean(GAIN_TOT), 
                                                             COUT_PLAN_MOY_M2 = mean( COUT_PLAN_MOY_M2)),
                  by=c("GESTE")]
data_couts_gestes[GAIN_TOT !=0 &  COUT_TOIT_MOY_M2 !=0 ,list(GAIN = mean(GAIN_TOT), 
                                                             COUT_TOIT_MOY_M2 = mean( COUT_TOIT_MOY_M2)),
                  by=c("GESTE")]


##### graph gain et coût total au ù²

ggplot(data_couts_gestes[GAIN_TOT !=0 & COUT_ENS_MOY_M2 !=0], aes(GAIN_TOT, COUT_ENS_MOY_M2, color = BRANCHE))+ geom_point()  + theme_bw() 
ggplot(data_couts_gestes[GAIN_TOT !=0 & COUT_ENS_MOY_M2 !=0], aes(GAIN_TOT, COUT_ENS_MOY_M2))+ geom_point()  + theme_bw() + facet_wrap(~BRANCHE) 



#### déterminants des gains
summary(lm(log(GAIN_TOT)~log(COUT_ENS_MOY_M2),data=data_couts_gestes[GAIN_TOT !=0& COUT_ENS_MOY_M2 !=0]))
summary(lm(log(GAIN_TOT)~log(COUT_ENS_MOY_M2)*BRANCHE ,data=data_couts_gestes[GAIN_TOT !=0& COUT_ENS_MOY_M2 !=0]))
summary(lm(log(GAIN_TOT)~log(COUT_ENS_MOY_M2)*BAT_TYPE,data=data_couts_gestes[GAIN_TOT !=0& COUT_ENS_MOY_M2 !=0]))

#### cout moyen par geste et branche


#### COUT DES FENETRES STATS

quantile(data_couts_gestes[ COUT_FEN_MOY_M2 !=0, COUT_FEN_MOY_M2])

data_couts_gestes[GAIN_TOT !=0 &  COUT_FEN_MOY_M2 !=0 ,list(COUT_FEN_MOY_M2 = mean( COUT_FEN_MOY_M2)),
                  by=c("GESTE","BRANCHE")]


COUT_FEN = data_couts_gestes[COUT_FEN_MOY_M2  !=0 & GESTE != "ENS_MAX",list(COUT_FEN_MOY_M2,BAT_TYPE, BRANCHE, COD_BRANCHE,PERIODE_DETAIL, GESTE),]

### coûts les plus bas
COUT_FEN[order(COUT_FEN_MOY_M2)][1:100]
data_couts_gestes[COUT_FEN_MOY_M2 !=0 & COUT_FEN_MOY_M2 < 20]
### coûts les plus hauts
COUT_FEN[order(COUT_FEN_MOY_M2,decreasing = T)][1:100]


ggplot(COUT_FEN[COD_BRANCHE == "01"],aes(BAT_TYPE,y=COUT_FEN_MOY_M2)) + geom_boxplot()
ggplot(COUT_FEN[COD_BRANCHE == "02"],aes(BAT_TYPE,y=COUT_FEN_MOY_M2)) + geom_boxplot() +
  theme(axis.text.x = element_text(angle = 45, vjust = 0.5))
ggplot(COUT_FEN[COD_BRANCHE == "03"],aes(BAT_TYPE,y=COUT_FEN_MOY_M2)) + geom_boxplot() +
  theme(axis.text.x = element_text(angle = 45, vjust = 0.5))
ggplot(COUT_FEN[COD_BRANCHE == "04"],aes(BAT_TYPE,y=COUT_FEN_MOY_M2)) + geom_boxplot() +
  theme(axis.text.x = element_text(angle = 45, vjust = 0.5))

ggplot(COUT_FEN,aes(BRANCHE,y=COUT_FEN_MOY_M2,fill = GESTE)) + geom_boxplot() +
  theme(axis.text.x = element_text(angle = 45, vjust = 0.5))

#### COUT DES MURS STATS 
quantile(data_couts_gestes[COUT_MUR_MOY_M2!=0,COUT_MUR_MOY_M2])
data_couts_gestes[GAIN_TOT !=0 &   COUT_MUR_MOY_M2  !=0 ,list(
                                                            COUT_MUR_MOY_M2  = mean(  COUT_MUR_MOY_M2 )),
                  by=c("GESTE","BRANCHE")]


COUT_MUR = data_couts_gestes[ COUT_MUR_MOY_M2  !=0,list(COUT_MUR_MOY_M2,BAT_TYPE, BRANCHE, COD_BRANCHE, GESTE),]

COUT_MUR[order(COUT_MUR_MOY_M2)][1:100]
COUT_MUR[order(COUT_MUR_MOY_M2,decreasing = T)][1:100]
ggplot(COUT_MUR,aes(BAT_TYPE,y=COUT_MUR_MOY_M2,fill = GESTE)) + geom_boxplot() +
  theme(axis.text.x = element_text(angle = 45, vjust = 0.5))

#### plancher et toit
quantile(data_couts_gestes[ COUT_PLAN_MOY_M2 !=0, COUT_PLAN_MOY_M2])
quantile(data_couts_gestes[ COUT_TOIT_MOY_M2 !=0, COUT_TOIT_MOY_M2])

data_couts_gestes[GAIN_TOT !=0 &   COUT_PLAN_MOY_M2  !=0 ,list(
                                                              COUT_PLAN_MOY_M2  = mean(  COUT_PLAN_MOY_M2 )),
                  by=c("GESTE","BRANCHE")]
COUT_PLAN = data_couts_gestes[ COUT_PLAN_MOY_M2  !=0,list(COUT_PLAN_MOY_M2,BAT_TYPE, BRANCHE, COD_BRANCHE,GESTE),]

COUT_PLAN[order(COUT_PLAN_MOY_M2)][1:100]
COUT_PLAN[order(COUT_PLAN_MOY_M2,decreasing = T)][1:100]


ggplot(COUT_PLAN,aes(BAT_TYPE,y=COUT_PLAN_MOY_M2, fill=GESTE)) + geom_boxplot() +
  theme(axis.text.x = element_text(angle = 45, vjust = 0.5))

data_couts_gestes[GAIN_TOT !=0 &   COUT_TOIT_MOY_M2  !=0 ,list(GAIN = mean(GAIN_TOT), 
                                                               COUT_TOIT_MOY_M2  = mean(  COUT_TOIT_MOY_M2 )),
                  by=c("GESTE","BRANCHE")]

COUT_TOIT = data_couts_gestes[ COUT_TOIT_MOY_M2  !=0,list(COUT_TOIT_MOY_M2,BAT_TYPE, BRANCHE, COD_BRANCHE, GESTE),]
COUT_TOIT[order(COUT_TOIT_MOY_M2)][1:100]
COUT_TOIT[order(COUT_TOIT_MOY_M2,decreasing = T)][1:100]
ggplot(COUT_TOIT,aes(BAT_TYPE,y=COUT_TOIT_MOY_M2, fill=GESTE)) + geom_boxplot() +
  theme(axis.text.x = element_text(angle = 45, vjust = 0.5))

#### coût ensemble du geste

COUT_ENS = data_couts_gestes[ COUT_ENS_MOY_M2  !=0,list(COUT_ENS_MOY_M2,GAIN_TOT,BAT_TYPE, BRANCHE, COD_BRANCHE, GESTE),]
quantile(COUT_ENS$COUT_ENS_MOY_M2)

COUT_ENS[order(COUT_ENS_MOY_M2)][1:100]
COUT_ENS[order(COUT_ENS_MOY_M2,decreasing = T)][1:100]

### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### 
######  recalage des coûts des parois pour le geste ENS_BBC en fonction de la différence de gain entre le geste modéré et le geste BBC ###### 
### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### 

data_couts_gestes_modif <- data_couts_gestes
data_couts_gestes_modif  <- melt(data_couts_gestes, id.vars = c("ID_AGREG","GESTE"), 
                                 measure.vars = c("COUT_TOIT_MOY_M2", "COUT_FEN_MOY_M2","COUT_MUR_MOY_M2",
                                                  "COUT_PLAN_MOY_M2","COUT_ENS_MOY_M2","GAIN_TOT"))

data_couts_gestes_modif  <- dcast.data.table(data_couts_gestes_modif  , ID_AGREG~variable +GESTE)


###### si les deux gestes existent
### on calcule le nouveau cout pour ENSBBC à partir des param de la courbe de coûts resirf
data_couts_gestes_modif[COUT_ENS_MOY_M2_ENS_MOD !=0 & COUT_ENS_MOY_M2_ENS_BBC !=0, 
                  COUT_ENS_MOY_M2_ENS_BBC_mod := COUT_ENS_MOY_M2_ENS_MOD*exp(2.85*(GAIN_TOT_ENS_BBC-GAIN_TOT_ENS_MOD))]



data_couts_gestes_modif[COUT_ENS_MOY_M2_ENS_MOD !=0 & COUT_ENS_MOY_M2_ENS_BBC !=0, 
                        Correctif_BBC :=  COUT_ENS_MOY_M2_ENS_BBC_mod/COUT_ENS_MOY_M2_ENS_BBC]


summary(data_couts_gestes_modif[COUT_ENS_MOY_M2_ENS_MOD !=0 & COUT_ENS_MOY_M2_ENS_BBC !=0,Correctif_BBC])

correctif_BBC_moy = mean(data_couts_gestes_modif[COUT_ENS_MOY_M2_ENS_MOD !=0 & COUT_ENS_MOY_M2_ENS_BBC !=0,Correctif_BBC])

### si seul le geste BBC existe, on applique le correctif moyen

summary(data_couts_gestes_modif[COUT_ENS_MOY_M2_ENS_MOD ==0 & COUT_ENS_MOY_M2_ENS_BBC !=0 ,])

data_couts_gestes_modif[COUT_ENS_MOY_M2_ENS_MOD ==0 & COUT_ENS_MOY_M2_ENS_BBC !=0 ,Correctif_BBC :=correctif_BBC_moy]
data_couts_gestes_modif[COUT_ENS_MOY_M2_ENS_MOD ==0 & COUT_ENS_MOY_M2_ENS_BBC !=0 , 
                        COUT_ENS_MOY_M2_ENS_BBC_mod := COUT_ENS_MOY_M2_ENS_BBC*Correctif_BBC]

data_couts_gestes_modif[is.na(Correctif_BBC)]
### on multiplie les coûts des gestes BBC par le même facteur
data_couts_gestes_modif[COUT_ENS_MOY_M2_ENS_BBC !=0, 
                        COUT_FEN_MOY_M2_ENS_BBC := COUT_FEN_MOY_M2_ENS_BBC*Correctif_BBC  ]

data_couts_gestes_modif[ COUT_ENS_MOY_M2_ENS_BBC !=0, 
                        COUT_MUR_MOY_M2_ENS_BBC := COUT_MUR_MOY_M2_ENS_BBC*Correctif_BBC  ]
data_couts_gestes_modif[ COUT_ENS_MOY_M2_ENS_BBC !=0, 
                        COUT_PLAN_MOY_M2_ENS_BBC := COUT_PLAN_MOY_M2_ENS_BBC*Correctif_BBC  ]
data_couts_gestes_modif[ COUT_ENS_MOY_M2_ENS_BBC !=0, 
                        COUT_TOIT_MOY_M2_ENS_BBC:= COUT_TOIT_MOY_M2_ENS_BBC*Correctif_BBC  ]

data_couts_gestes_modif[ COUT_ENS_MOY_M2_ENS_BBC !=0, 
                        COUT_ENS_MOY_M2_ENS_BBC := COUT_ENS_MOY_M2_ENS_BBC_mod  ]
data_couts_gestes_modif[,COUT_ENS_MOY_M2_ENS_BBC_mod:=NULL]

##### verif 
data_couts_gestes_modif[COUT_ENS_MOY_M2_ENS_BBC !=0, 
                        COUT_FEN_MOY_M2_ENS_BBC+  COUT_MUR_MOY_M2_ENS_BBC +  COUT_PLAN_MOY_M2_ENS_BBC + COUT_TOIT_MOY_M2_ENS_BBC 
                        - COUT_ENS_MOY_M2_ENS_BBC] %>% round()

#### on remplace les coûts modifiés dans la base initiale


setnames(data_couts_gestes_modif,paste(c("COUT_TOIT_MOY_M2", "COUT_FEN_MOY_M2","COUT_MUR_MOY_M2",
      "COUT_PLAN_MOY_M2","COUT_ENS_MOY_M2","GAIN_TOT"), 
      c(rep("Etat initial",6),rep("ENS_MOD",6),rep("ENS_BBC",6),rep("ENS_MAX",6)),sep="_"),
      paste(c("COUTTOITMOYM2", "COUTFENMOYM2","COUTMURMOYM2",
        "COUTPLANMOYM2","COUTENSMOYM2","GAINTOT"), 
      c(rep("Etat initial",6),rep("ENSMOD",6),rep("ENSBBC",6),rep("ENSMAX",6)),sep="_"))

data_couts_gestes_modif  <- melt(data_couts_gestes_modif,id.vars = c("ID_AGREG","Correctif_BBC"))
data_couts_gestes_modif[,var_name := unlist(lapply(strsplit(as.character(variable),split = "_"),"[",1)),]
data_couts_gestes_modif[,GESTE := unlist(lapply(strsplit(as.character(variable),split = "_"),"[",2)),]
data_couts_gestes_modif[, var_name := factor(var_name,levels = c("COUTTOITMOYM2", "COUTFENMOYM2","COUTMURMOYM2",
                                                                  "COUTPLANMOYM2","COUTENSMOYM2","GAINTOT"))]

data_couts_gestes_modif[, GESTE := parse_factor(GESTE, levels= unique(GESTE))]

data_couts_gestes_modif[, var_name := fct_recode(var_name, 
                                                  "COUT_TOIT_MOY_M2" = "COUTTOITMOYM2",
                                                  "COUT_FEN_MOY_M2" = "COUTFENMOYM2" ,
                                                 "COUT_MUR_MOY_M2" = "COUTMURMOYM2",
                                                 "COUT_PLAN_MOY_M2"="COUTPLANMOYM2",
                                                 "COUT_ENS_MOY_M2"="COUTENSMOYM2",
                                                 "GAIN_TOT" = "GAINTOT"
                                                 )]
data_couts_gestes_modif[,GESTE := fct_recode(GESTE, 
                                                 "ENS_MOD" = "ENSMOD",
                                                 "ENS_BBC" = "ENSBBC" ,
                                                 "ENS_MAX" = "ENSMAX")]
data_couts_gestes_modif <- dcast.data.table(data_couts_gestes_modif, ID_AGREG + GESTE ~ var_name, value.var="value")
  


data_couts_gestes2 <- data_couts_gestes

data_couts_gestes2 <- merge(data_couts_gestes2[,names(data_couts_gestes2)[names(data_couts_gestes2) %in% c("COUT_TOIT_MOY_M2", "COUT_FEN_MOY_M2","COUT_MUR_MOY_M2",
                                                                                  "COUT_PLAN_MOY_M2","COUT_ENS_MOY_M2","GAIN_TOT") ==F], with=F],
                            data_couts_gestes_modif,by=c("ID_AGREG","GESTE"))

data_couts_gestes_modif[,BRANCHE := substring(ID_AGREG,0,2)]

data_couts_gestes_save[ID_AGREG == "0101420701"]
data_couts_gestes2[ID_AGREG == "0101420701"]


#### coût ensemble du geste après modif
COUT_ENS2 = data_couts_gestes2[ COUT_ENS_MOY_M2  !=0,list(COUT_ENS_MOY_M2,BAT_TYPE, BRANCHE, COD_BRANCHE, GESTE, GAIN_TOT),]
COUT_ENS2[order(COUT_ENS_MOY_M2)][1:100]
COUT_ENS2[order(COUT_ENS_MOY_M2,decreasing = T)][1:100]
ggplot(COUT_ENS2,aes(BAT_TYPE,y=COUT_ENS_MOY_M2, fill=GESTE)) + geom_boxplot() +
  theme(axis.text.x = element_text(angle = 45, vjust = 0.5))

ggplot(COUT_ENS2[GESTE!="ENS_MAX"], aes(GAIN_TOT,COUT_ENS_MOY_M2, color=GESTE))+geom_point()

COUT_ENS2[,list(mean(COUT_ENS_MOY_M2),mean(GAIN_TOT)),by="GESTE"]

###### tableau des gestes pour la publi

COUT_ENS2[GESTE %in% c("ENS_MOD","ENS_BBC"),list(mean(COUT_ENS_MOY_M2),mean(GAIN_TOT)),by=c("BRANCHE","GESTE")]
COUT_ENS2[GESTE %in% c("ENS_MOD","ENS_BBC"),list(mean(COUT_ENS_MOY_M2),mean(GAIN_TOT)),by=c("GESTE")]

data_couts_gestes <- data_couts_gestes2

### ### ### ### ### ### ### ### ### ### ### ### ### 
######  repartitions des gains par paroi  ###### 
### ### ### ### ### ### ### ### ### ### ### ### 

summary(repart_gain)
data_couts_gestes = merge(data_couts_gestes, repart_gain[,c("BRANCHE","SS_BRANCHE","BAT_TYPE",
                                                               "Smur/Sparois","Sfen/Sparois", "Stoit/Sparois",
                                                               "Splancher/Sparois" ),with=F], 
                                                         by=c("BRANCHE","BAT_TYPE", "SS_BRANCHE"))


#### on répartit les gains en fonction de la part de la surface de la paroi dans l'ensemble des surfaces

data_couts_gestes[, REPART_FEN := get("Sfen/Sparois")]
data_couts_gestes[, REPART_MUR := get("Smur/Sparois")]
data_couts_gestes[, REPART_PLAN := get("Splancher/Sparois")]
data_couts_gestes[, REPART_TOIT := get("Stoit/Sparois")]

data_couts_gestes[, REPART_PLANTOIT := REPART_PLAN + REPART_TOIT]
data_couts_gestes[, REPART_FENMUR := REPART_FEN + REPART_MUR]
data_couts_gestes[, REPART_ENS := REPART_PLANTOIT + REPART_FENMUR]

summary(data_couts_gestes)

### Répartition reste gains entre plancher et toiture 
partTOIT = 1/2
allvars = c("REPART_FEN","REPART_MUR", "REPART_PLAN", "REPART_TOIT", 
            "REPART_PLANTOIT","REPART_FENMUR","REPART_ENS")


### si coût nul pour une paroi, gain nul
data_couts_gestes[COUT_MUR_MOY_M2 == 0, REPART_MUR := 0]
data_couts_gestes[COUT_FEN_MOY_M2 == 0, REPART_FEN := 0]
data_couts_gestes[COUT_PLAN_MOY_M2 == 0 , REPART_PLAN := 0]
data_couts_gestes[COUT_TOIT_MOY_M2 == 0, REPART_TOIT := 0]
data_couts_gestes[COUT_TOIT_MOY_M2 == 0 & COUT_PLAN_MOY_M2 == 0, REPART_PLANTOIT := 0]
data_couts_gestes[COUT_FEN_MOY_M2 == 0 & COUT_MUR_MOY_M2 == 0, REPART_FENMUR := 0]
data_couts_gestes[COUT_FEN_MOY_M2 == 0 & COUT_MUR_MOY_M2 == 0 & COUT_TOIT_MOY_M2 == 0 & COUT_PLAN_MOY_M2 == 0,
                  REPART_ENS := 0]

### cas où une seule paroi est changée, on impute tout le gain au geste ensemble qui est le seul geste restant

### plancher seul, geste = ENSEMBLE
data_couts_gestes[GAIN_TOT != 0 & COUT_PLAN_MOY_M2 !=0 & COUT_FEN_MOY_M2 ==0 & COUT_MUR_MOY_M2 ==0 & 
                    COUT_TOIT_MOY_M2 ==0,]


data_couts_gestes[GAIN_TOT != 0 & COUT_PLAN_MOY_M2 !=0 & COUT_FEN_MOY_M2 ==0 & COUT_MUR_MOY_M2 ==0 & 
                    COUT_TOIT_MOY_M2 ==0, 
                  REPART_PLAN := 1]

data_couts_gestes[GAIN_TOT != 0 & COUT_PLAN_MOY_M2 !=0 & COUT_FEN_MOY_M2 ==0 & COUT_MUR_MOY_M2 ==0 & 
                    COUT_TOIT_MOY_M2 ==0, 
                  REPART_PLANTOIT := 1]

data_couts_gestes[GAIN_TOT != 0 & COUT_PLAN_MOY_M2 !=0 & COUT_FEN_MOY_M2 ==0 & COUT_MUR_MOY_M2 ==0 & 
                    COUT_TOIT_MOY_M2 ==0, 
                  REPART_ENS := 1]

data_couts_gestes[GAIN_TOT != 0 & COUT_PLAN_MOY_M2 !=0 & COUT_FEN_MOY_M2 ==0 & COUT_MUR_MOY_M2 ==0 & 
                    COUT_TOIT_MOY_M2 ==0,]

### fenetres seules, gestes = FEN
data_couts_gestes[GAIN_TOT != 0 & COUT_PLAN_MOY_M2 ==0 & COUT_FEN_MOY_M2 !=0 & COUT_MUR_MOY_M2 ==0 & 
                    COUT_TOIT_MOY_M2 ==0, ]

data_couts_gestes[GAIN_TOT != 0 & COUT_PLAN_MOY_M2 ==0 & COUT_FEN_MOY_M2 !=0 & COUT_MUR_MOY_M2 ==0 & 
                    COUT_TOIT_MOY_M2 ==0, 
                  REPART_FEN := 1]

data_couts_gestes[GAIN_TOT != 0 & COUT_PLAN_MOY_M2 ==0 & COUT_FEN_MOY_M2 !=0 & COUT_MUR_MOY_M2 ==0 & 
                    COUT_TOIT_MOY_M2 ==0, 
                  REPART_FENMUR := 0]

data_couts_gestes[GAIN_TOT != 0 & COUT_PLAN_MOY_M2 ==0 & COUT_FEN_MOY_M2 !=0 & COUT_MUR_MOY_M2 ==0 & 
                    COUT_TOIT_MOY_M2 ==0, 
                  REPART_ENS := 0]

data_couts_gestes[GAIN_TOT != 0 & COUT_PLAN_MOY_M2 ==0 & COUT_FEN_MOY_M2 !=0 & COUT_MUR_MOY_M2 ==0 & 
                    COUT_TOIT_MOY_M2 ==0, ]

### murs seuls, geste = FENMUR
data_couts_gestes[GAIN_TOT != 0 & COUT_PLAN_MOY_M2 ==0 & COUT_FEN_MOY_M2 ==0 & COUT_MUR_MOY_M2 !=0 & 
                    COUT_TOIT_MOY_M2 ==0, 
                  ]

data_couts_gestes[GAIN_TOT != 0 & COUT_PLAN_MOY_M2 ==0 & COUT_FEN_MOY_M2 ==0 & COUT_MUR_MOY_M2 !=0 & 
                    COUT_TOIT_MOY_M2 ==0, 
                  REPART_MUR := 1]

data_couts_gestes[GAIN_TOT != 0 & COUT_PLAN_MOY_M2 ==0 & COUT_FEN_MOY_M2 ==0 & COUT_MUR_MOY_M2 !=0 & 
                    COUT_TOIT_MOY_M2 ==0, 
                  REPART_FENMUR := 1]

data_couts_gestes[GAIN_TOT != 0 & COUT_PLAN_MOY_M2 ==0 & COUT_FEN_MOY_M2 ==0 & COUT_MUR_MOY_M2 !=0 & 
                    COUT_TOIT_MOY_M2 ==0, 
                  REPART_ENS := 0]

data_couts_gestes[GAIN_TOT != 0 & COUT_PLAN_MOY_M2 ==0 & COUT_FEN_MOY_M2 ==0 & COUT_MUR_MOY_M2 !=0 & 
                    COUT_TOIT_MOY_M2 ==0, 
                 ]

### toiture seule, geste = ENS
data_couts_gestes[GAIN_TOT != 0 & COUT_PLAN_MOY_M2 ==0 & COUT_FEN_MOY_M2 ==0 & COUT_MUR_MOY_M2 ==0 & 
                    COUT_TOIT_MOY_M2 !=0, ]
data_couts_gestes[GAIN_TOT != 0 & COUT_PLAN_MOY_M2 ==0 & COUT_FEN_MOY_M2 ==0 & COUT_MUR_MOY_M2 ==0 & 
                    COUT_TOIT_MOY_M2 !=0, 
                  REPART_TOIT := 1]

data_couts_gestes[GAIN_TOT != 0 & COUT_PLAN_MOY_M2 ==0 & COUT_FEN_MOY_M2 ==0 & COUT_MUR_MOY_M2 ==0 & 
                    COUT_TOIT_MOY_M2 !=0, 
                  REPART_PLANTOIT := 1]

data_couts_gestes[GAIN_TOT != 0 & COUT_PLAN_MOY_M2 ==0 & COUT_FEN_MOY_M2 ==0 & COUT_MUR_MOY_M2 ==0 & 
                    COUT_TOIT_MOY_M2 !=0, 
                  REPART_ENS := 1]

data_couts_gestes[GAIN_TOT != 0 & COUT_PLAN_MOY_M2 ==0 & COUT_FEN_MOY_M2 ==0 & COUT_MUR_MOY_M2 ==0 & 
                    COUT_TOIT_MOY_M2 !=0, ]

#### cas où il n'y pas de changement de toiture et de plancher, 
#### on redistribue aux fenêtres et aux murs en fonction de leur part dans l'ensemble fenêtres + murs
#### le geste ensemble n'est pas retenu

data_couts_gestes[GAIN_TOT != 0 & COUT_PLAN_MOY_M2 ==0 & COUT_FEN_MOY_M2 !=0 & COUT_MUR_MOY_M2 !=0 & 
                    COUT_TOIT_MOY_M2 ==0, ]

data_couts_gestes[GAIN_TOT != 0 & COUT_PLAN_MOY_M2 ==0 & COUT_FEN_MOY_M2 !=0 & COUT_MUR_MOY_M2 !=0 & 
                    COUT_TOIT_MOY_M2 ==0, 
                  REPART_FEN := REPART_FEN/REPART_FENMUR]

data_couts_gestes[GAIN_TOT != 0 & COUT_PLAN_MOY_M2 ==0 & COUT_FEN_MOY_M2 !=0 & COUT_MUR_MOY_M2 !=0 & 
                    COUT_TOIT_MOY_M2 ==0, 
                  REPART_MUR := REPART_MUR/REPART_FENMUR]

data_couts_gestes[GAIN_TOT != 0 & COUT_PLAN_MOY_M2 ==0 & COUT_FEN_MOY_M2 !=0 & COUT_MUR_MOY_M2 !=0 & 
                    COUT_TOIT_MOY_M2 ==0, 
                  REPART_FENMUR := 1]

data_couts_gestes[GAIN_TOT != 0 & COUT_PLAN_MOY_M2 ==0 & COUT_FEN_MOY_M2 !=0 & COUT_MUR_MOY_M2 !=0 & 
                    COUT_TOIT_MOY_M2 ==0, 
                  REPART_ENS := 0]

data_couts_gestes[GAIN_TOT != 0 & COUT_PLAN_MOY_M2 ==0 & COUT_FEN_MOY_M2 !=0 & COUT_MUR_MOY_M2 !=0 & 
                    COUT_TOIT_MOY_M2 ==0, ]

#### cas où il n'y pas de changement de fenêtres et de murs, 
#### on redistribue aux plancher et aux en fonction de leur part dans l'ensemble plancher + toiture, 
#### le geste ensemble est le seul retenu 

data_couts_gestes[GAIN_TOT != 0 & COUT_PLAN_MOY_M2 !=0 & COUT_FEN_MOY_M2 ==0 & COUT_MUR_MOY_M2 ==0 & 
                    COUT_TOIT_MOY_M2 !=0,]

data_couts_gestes[GAIN_TOT != 0 & COUT_PLAN_MOY_M2 !=0 & COUT_FEN_MOY_M2 ==0 & COUT_MUR_MOY_M2 ==0 & 
                    COUT_TOIT_MOY_M2 !=0, 
                  REPART_TOIT := partTOIT]

data_couts_gestes[GAIN_TOT != 0 & COUT_PLAN_MOY_M2 !=0 & COUT_FEN_MOY_M2 ==0 & COUT_MUR_MOY_M2 ==0 & 
                    COUT_TOIT_MOY_M2 !=0, 
                  REPART_PLAN := 1-partTOIT]

data_couts_gestes[GAIN_TOT != 0 & COUT_PLAN_MOY_M2 !=0 & COUT_FEN_MOY_M2 ==0 & COUT_MUR_MOY_M2 ==0 & 
                    COUT_TOIT_MOY_M2 !=0, 
                  REPART_PLANTOIT := 1]

data_couts_gestes[GAIN_TOT != 0 & COUT_PLAN_MOY_M2 !=0 & COUT_FEN_MOY_M2 ==0 & COUT_MUR_MOY_M2 ==0 & 
                    COUT_TOIT_MOY_M2 !=0, 
                  REPART_ENS := 1]

data_couts_gestes[GAIN_TOT != 0 & COUT_PLAN_MOY_M2 !=0 & COUT_FEN_MOY_M2 ==0 & COUT_MUR_MOY_M2 ==0 & 
                    COUT_TOIT_MOY_M2 !=0,]

#### cas où il n'y pas de changement de fenêtres mais changement de murs, la part de murs + fenetres est égale à la part des murs
#### le reste va aux placnher et toiture

data_couts_gestes[GAIN_TOT != 0 &  COUT_FEN_MOY_M2 ==0 & COUT_MUR_MOY_M2 !=0  & (COUT_PLAN_MOY_M2 !=0 | COUT_TOIT_MOY_M2 !=0), ]

data_couts_gestes[GAIN_TOT != 0 &  COUT_FEN_MOY_M2 ==0 & COUT_MUR_MOY_M2 !=0  & (COUT_PLAN_MOY_M2 !=0 | COUT_TOIT_MOY_M2 !=0), 
                  REPART_MUR :=   REPART_FENMUR ]

data_couts_gestes[GAIN_TOT != 0 &  COUT_FEN_MOY_M2 ==0 & COUT_MUR_MOY_M2 !=0 & (COUT_PLAN_MOY_M2 !=0 | COUT_TOIT_MOY_M2 !=0), 
                  REPART_PLANTOIT := 1 - REPART_FENMUR ]

data_couts_gestes[GAIN_TOT != 0 &  COUT_FEN_MOY_M2 ==0 & COUT_MUR_MOY_M2 !=0 & COUT_PLAN_MOY_M2 !=0, 
                  REPART_PLAN := REPART_PLANTOIT*(1-partTOIT)]
data_couts_gestes[GAIN_TOT != 0 &  COUT_FEN_MOY_M2 ==0 & COUT_MUR_MOY_M2 !=0 & COUT_TOIT_MOY_M2 !=0, 
                  REPART_TOIT := REPART_PLANTOIT*(partTOIT)]

data_couts_gestes[GAIN_TOT != 0 &  COUT_FEN_MOY_M2 ==0 & COUT_MUR_MOY_M2 !=0 & COUT_PLAN_MOY_M2 ==0, 
                  REPART_PLAN := 0]
data_couts_gestes[GAIN_TOT != 0 &  COUT_FEN_MOY_M2 ==0 & COUT_MUR_MOY_M2 !=0 & COUT_PLAN_MOY_M2 ==0 & COUT_TOIT_MOY_M2 !=0, 
                  REPART_TOIT := REPART_PLANTOIT]

data_couts_gestes[GAIN_TOT != 0 &  COUT_FEN_MOY_M2 ==0 & COUT_MUR_MOY_M2 !=0 & COUT_TOIT_MOY_M2 ==0 , 
                  REPART_TOIT := 0]
data_couts_gestes[GAIN_TOT != 0 &  COUT_FEN_MOY_M2 ==0 & COUT_MUR_MOY_M2 !=0 & COUT_TOIT_MOY_M2 ==0 & COUT_PLAN_MOY_M2 !=0, 
                  REPART_PLAN := REPART_PLANTOIT]


data_couts_gestes[GAIN_TOT != 0 &  COUT_FEN_MOY_M2 ==0 & COUT_MUR_MOY_M2 !=0 &  REPART_FENMUR == 1 , 
                  REPART_ENS := 0 ]
data_couts_gestes[GAIN_TOT != 0 &  COUT_FEN_MOY_M2 ==0 & COUT_MUR_MOY_M2 !=0 &  REPART_FENMUR != 1, 
                  REPART_ENS := 1 ]
data_couts_gestes[GAIN_TOT != 0 &  COUT_FEN_MOY_M2 ==0 & COUT_MUR_MOY_M2 !=0  & (COUT_PLAN_MOY_M2 !=0 | COUT_TOIT_MOY_M2 !=0), ]

#### cas où il y a changement de fenêtres mais pas changement de murs, le geste fenêtres + mur n'est pas sélectionné, 
#### plancher et toiture prenne le reste des fenetres

data_couts_gestes[GAIN_TOT != 0 &  COUT_FEN_MOY_M2 !=0 & COUT_MUR_MOY_M2 ==0 & (COUT_PLAN_MOY_M2 !=0 | COUT_TOIT_MOY_M2 !=0),]

data_couts_gestes[GAIN_TOT != 0 &   COUT_FEN_MOY_M2 !=0 & COUT_MUR_MOY_M2 ==0  & (COUT_PLAN_MOY_M2 !=0 | COUT_TOIT_MOY_M2 !=0),
                  REPART_FEN := REPART_FENMUR]
data_couts_gestes[GAIN_TOT != 0 &  COUT_FEN_MOY_M2 !=0 & COUT_MUR_MOY_M2 ==0  & (COUT_PLAN_MOY_M2 !=0 | COUT_TOIT_MOY_M2 !=0), 
                  REPART_FENMUR := 0 ]

data_couts_gestes[GAIN_TOT != 0 &   COUT_FEN_MOY_M2 !=0 & COUT_MUR_MOY_M2 ==0  & (COUT_PLAN_MOY_M2 !=0 | COUT_TOIT_MOY_M2 !=0),
                  REPART_PLANTOIT := 1 - REPART_FEN ]

data_couts_gestes[GAIN_TOT != 0 &   COUT_FEN_MOY_M2 !=0 & COUT_MUR_MOY_M2 ==0 & COUT_PLAN_MOY_M2 !=0,
                  REPART_PLAN := REPART_PLANTOIT*(1-partTOIT)]
data_couts_gestes[GAIN_TOT != 0 &   COUT_FEN_MOY_M2 !=0 & COUT_MUR_MOY_M2 ==0 & COUT_TOIT_MOY_M2 !=0,
                  REPART_TOIT := REPART_PLANTOIT*(partTOIT)]

data_couts_gestes[GAIN_TOT != 0 &    COUT_FEN_MOY_M2 !=0 & COUT_MUR_MOY_M2 ==0 & COUT_PLAN_MOY_M2 ==0, 
                  REPART_PLAN := 0]
data_couts_gestes[GAIN_TOT != 0 &    COUT_FEN_MOY_M2 !=0 & COUT_MUR_MOY_M2 ==0 & COUT_PLAN_MOY_M2 ==0 & COUT_TOIT_MOY_M2 !=0, 
                  REPART_TOIT := REPART_PLANTOIT]
data_couts_gestes[GAIN_TOT != 0 &    COUT_FEN_MOY_M2 !=0 & COUT_MUR_MOY_M2 ==0 & COUT_TOIT_MOY_M2 ==0 , 
                  REPART_TOIT := 0]
data_couts_gestes[GAIN_TOT != 0 &   COUT_FEN_MOY_M2 !=0 & COUT_MUR_MOY_M2 ==0 & COUT_TOIT_MOY_M2 ==0 & COUT_PLAN_MOY_M2 !=0, 
                  REPART_PLAN := REPART_PLANTOIT]

data_couts_gestes[GAIN_TOT != 0 &  COUT_FEN_MOY_M2 !=0 & COUT_MUR_MOY_M2 ==0 &  REPART_FEN == 1, 
                  REPART_ENS :=  0 ]
data_couts_gestes[GAIN_TOT != 0 &  COUT_FEN_MOY_M2 !=0 & COUT_MUR_MOY_M2 ==0 &  REPART_FEN != 1, 
                  REPART_ENS :=  1 ]

data_couts_gestes[GAIN_TOT != 0 &  COUT_FEN_MOY_M2 !=0 & COUT_MUR_MOY_M2 ==0 & (COUT_PLAN_MOY_M2 !=0 | COUT_TOIT_MOY_M2 !=0),]


#### cas où pas de toiture ou pas de plancher mais l'autre oui, on donne la partie toiture + plancher aux toires ou aux plachers

data_couts_gestes[GAIN_TOT != 0 &  COUT_TOIT_MOY_M2 ==0 & COUT_PLAN_MOY_M2 != 0, REPART_PLAN := REPART_PLANTOIT]
data_couts_gestes[GAIN_TOT != 0 &  COUT_TOIT_MOY_M2 ==0 & COUT_PLAN_MOY_M2 != 0, REPART_TOIT := 0]
data_couts_gestes[GAIN_TOT != 0 &  COUT_TOIT_MOY_M2 !=0 & COUT_PLAN_MOY_M2 == 0, REPART_TOIT := REPART_PLANTOIT]
data_couts_gestes[GAIN_TOT != 0 &  COUT_TOIT_MOY_M2 !=0 & COUT_PLAN_MOY_M2 == 0, REPART_PLAN := 0]

#### verif coût nul et répartition non nulle
data_couts_gestes[COUT_MUR_MOY_M2 == 0 & REPART_MUR != 0]
data_couts_gestes[COUT_FEN_MOY_M2 == 0 & REPART_FEN != 0]
data_couts_gestes[COUT_PLAN_MOY_M2 == 0 & REPART_PLAN != 0]
data_couts_gestes[COUT_TOIT_MOY_M2 == 0 & REPART_TOIT != 0]


#### verif coût non nul et répartition nulle 
data_couts_gestes[COUT_MUR_MOY_M2 != 0 & REPART_MUR == 0]
data_couts_gestes[COUT_FEN_MOY_M2 != 0 & REPART_FEN == 0]
data_couts_gestes[COUT_PLAN_MOY_M2 != 0 & REPART_PLAN == 0]
data_couts_gestes[COUT_TOIT_MOY_M2 != 0 & REPART_TOIT == 0]


#### verifcoût ensemble non nulle et répartition différente de 100 %
data_couts_gestes[GAIN_TOT != 0 & COUT_ENS_MOY_M2 != 0 & (REPART_FEN + REPART_MUR + REPART_TOIT+ REPART_PLAN >1.01),]
data_couts_gestes[GAIN_TOT != 0 & COUT_ENS_MOY_M2 != 0 & (REPART_FEN + REPART_MUR + REPART_TOIT+ REPART_PLAN <0.99),]


data_couts_gestes[GAIN_TOT != 0 & COUT_ENS_MOY_M2 != 0 & REPART_FENMUR != 0 &  (REPART_FEN + REPART_MUR - REPART_FENMUR>0.01),]
data_couts_gestes[GAIN_TOT != 0 & COUT_ENS_MOY_M2 != 0 & REPART_FENMUR != 0 &  (REPART_FEN + REPART_MUR - REPART_FENMUR< -0.01),]

data_couts_gestes[GAIN_TOT != 0 & COUT_ENS_MOY_M2 != 0 & (REPART_TOIT + REPART_PLAN -REPART_PLANTOIT > 0.01),]
data_couts_gestes[GAIN_TOT != 0 & COUT_ENS_MOY_M2 != 0 & (REPART_TOIT + REPART_PLAN -REPART_PLANTOIT < -0.01),]

data_couts_gestes[GAIN_TOT != 0 & COUT_ENS_MOY_M2 != 0 & REPART_FEN == 1 & REPART_ENS == 1,]
data_couts_gestes[GAIN_TOT != 0 & COUT_ENS_MOY_M2 != 0 & REPART_FENMUR == 1 & REPART_ENS == 1,]
data_couts_gestes[GAIN_TOT != 0 & COUT_ENS_MOY_M2 != 0 &  REPART_ENS == 0 & REPART_FEN != 1 & REPART_FENMUR != 1,]


###############################################
#### calculs gain par parois et par gestes
###############################################

data_couts_gestes[,GAIN_FEN := GAIN_TOT*REPART_FEN]
data_couts_gestes[,GAIN_FENMUR := GAIN_TOT*REPART_FENMUR]
data_couts_gestes[,GAIN_ENS := GAIN_TOT*REPART_ENS]

data_couts_gestes[GAIN_ENS - GAIN_TOT >0.01 & GAIN_ENS!=0]
data_couts_gestes[GAIN_ENS - GAIN_TOT < -0.01 & GAIN_ENS!=0]

#### gestes avec des coûts mais pas de gains
data_couts_gestes[GAIN_TOT == 0 & GESTE != "Etat initial"& COUT_ENS_MOY_M2!=0]

nrow(data_couts_gestes[GAIN_TOT == 0 & GESTE != "Etat initial"& COUT_ENS_MOY_M2!=0])

#### on impute le gain avec le gain RESIRF issue de la courbe liant le gain et le cout au m²
data_couts_gestes[GAIN_TOT == 0 & GESTE != "Etat initial"& COUT_ENS_MOY_M2!=0,  (1/2.8543)*log(COUT_ENS_MOY_M2/27.31)]
data_couts_gestes[GAIN_TOT == 0 & GESTE != "Etat initial"& COUT_ENS_MOY_M2!=0, GAIN_TOT := (1/2.8543)*log(COUT_ENS_MOY_M2/27.31)]

data_couts_gestes[,GAIN_FEN := GAIN_TOT*REPART_FEN]
data_couts_gestes[,GAIN_FENMUR := GAIN_TOT*REPART_FENMUR]
data_couts_gestes[,GAIN_ENS := GAIN_TOT*REPART_ENS]

data_couts_gestes[COUT_FEN_MOY_M2 != 0 & GAIN_FEN== 0  ]


data_couts_gestes[COUT_FENMUR_MOY_M2 != 0 & COUT_MUR_MOY_M2 != 0  & GAIN_FENMUR== 0  ]


#### On vire les gestes avec gain nul
data_couts_gestes[ GAIN_TOT == 0 & GESTE != "Etat initial",]
data_couts_gestes[ GAIN_TOT == 0 , COUT_FEN_MOY_M2 := 0 ]
data_couts_gestes[ GAIN_TOT == 0 , COUT_FENMUR_MOY_M2 := 0 ]
data_couts_gestes[ GAIN_FEN == 0 , COUT_FEN_MOY_M2 := 0 ]
data_couts_gestes[ GAIN_FENMUR == 0 , COUT_FENMUR_MOY_M2 := 0 ]
data_couts_gestes[ GAIN_ENS == 0 , COUT_ENS_MOY_M2 := 0 ]

#### création table finale input modèle tertiaire

data_couts_gestes[GESTE == "ENS_MOD",EXIGENCE := "RT par element" ]
data_couts_gestes[GESTE == "ENS_MOD",EXIGENCE := "RT par element" ]
data_couts_gestes[GESTE == "ENS_BBC",EXIGENCE := "BBC renovation" ]
data_couts_gestes[GESTE == "ENS_MAX",EXIGENCE := "Maximale" ]
data_couts_gestes[GESTE == "Etat initial",EXIGENCE := "Etat initial" ]

summary(data_couts_gestes)

### ### ### ### ### ### ### ### ### ### ### ### 
##### AJOUTS DES valeurs forfaits CEE ######
### ### ### ### ### ### ### ### ### ### ### ### 

# ##### modif CEE à partir de ADEME gisement
# data_couts_gestes[,CEE_ADEME := 0]
# data_couts_gestes[COUT_PLAN_MOY_M2 > 0, CEE_PLAN :=  296147/80]
# data_couts_gestes[COUT_FEN_MOY_M2 > 0, CEE_FEN := 31566/80]
# data_couts_gestes[COUT_MUR_MOY_M2 > 0, CEE_MUR :=  204441/80]
# data_couts_gestes[COUT_TOIT_MOY_M2 > 0, CEE_TOIT := 150770/80]

##### ajouts CEE à partir des fiches  

CEE_recalc = fread("../AME_AMS_dataDGEC/CEE/bibli_chauffage_CEE.csv",dec = ",")
data_couts_gestes = merge(data_couts_gestes,CEE_recalc[,list(BRANCHE, SS_BRANCHE, BAT_TYPE,CEE_TOIT,CEE_PLAN, CEE_FEN,CEE_MUR)], 
                          by=c("BRANCHE","SS_BRANCHE","BAT_TYPE"), all.x = T)


data_couts_gestes[COUT_PLAN_MOY_M2 == 0, CEE_PLAN :=  0]
data_couts_gestes[COUT_FEN_MOY_M2 == 0, CEE_FEN := 0]
data_couts_gestes[COUT_MUR_MOY_M2 == 0, CEE_MUR :=  0]
data_couts_gestes[COUT_TOIT_MOY_M2== 0, CEE_TOIT := 0]

data_couts_gestes[GAIN_FEN >0,CEE_FEN := CEE_FEN]
data_couts_gestes[GAIN_FENMUR >0,CEE_FENMUR := CEE_FEN +CEE_MUR]
data_couts_gestes[GAIN_ENS >0,CEE_ENS := CEE_FEN +CEE_MUR +  CEE_PLAN + CEE_TOIT]

summary(data_couts_gestes)

##### table finale #####

bibli_couts =  data_couts_gestes[EXIGENCE != "Etat initial",list(ID_AGREG,EXIGENCE,
                        COUT_FEN = COUT_FEN_MOY_M2 ,COUT_FENMUR = COUT_FENMUR_MOY_M2, COUT_ENS = COUT_ENS_MOY_M2,
                        GAIN_FEN,GAIN_FENMUR, GAIN_ENS,CEE_FEN,CEE_FENMUR, CEE_ENS )]

bibli_couts = melt(bibli_couts[!is.na(EXIGENCE)],id.vars = c("ID_AGREG","EXIGENCE"))
bibli_couts[,GESTE := as.character(lapply(strsplit(as.character(variable), split = "_"),"[", 2))]
bibli_couts[,variable := as.character(lapply(strsplit(as.character(variable), split = "_"),"[", 1))]


#### modifie le nom des gestes pour correspondre au modèle

bibli_couts[GESTE == "FENMUR",GESTE :="FEN_MUR"]

bibli_couts[EXIGENCE == "RT par element",GESTE := paste(GESTE,"MOD",sep="")]
bibli_couts[EXIGENCE == "BBC renovation",GESTE := paste(GESTE,"BBC",sep="")]
bibli_couts[EXIGENCE == "Maximale",GESTE := paste(GESTE,"MAX",sep="")]

bibli_couts = dcast.data.table(bibli_couts , ID_AGREG + EXIGENCE + GESTE  ~ variable)

bibli_couts[is.na(COUT)]

##### ajoute geste GTB (gain  = 15% COUT = 20 euros par m²)
GTB = data.table(ID_AGREG = unique(bibli_couts$ID_AGREG),EXIGENCE = "GTB",
                 GESTE = "GTB","COUT" = 20, GAIN=0.15, CEE = 0)

bibli_couts = rbind(bibli_couts, GTB)

bibli_couts[is.na(CEE), CEE:=0]

##### comparaison avec l'ancienne bibli
bibli_old =  fread("table_param_origine/Bibli_geste_bati.csv", 
                   colClasses = list("character" = c("ID_AGREG","EXIGENCE","GESTE")))

bibli_couts_comp = merge(bibli_couts, bibli_old[,list(CEE_old=CEE, ID_AGREG, GESTE, GAIN_old = GAIN, COUT_old = COUT)], by=c("ID_AGREG", "GESTE"), all.x=T)


bibli_couts_comp[is.na(COUT)]

#### données présentes avant et absentes maintenant 
bibli_couts_comp[ EXIGENCE != "Maximale" & EXIGENCE != "Etat initial" & COUT == 0 & GAIN == 0 & GAIN_old!=0]
unique(bibli_couts_comp[ EXIGENCE != "Maximale" & EXIGENCE != "Etat initial" & COUT == 0 & GAIN == 0 & GAIN_old!=0, substring(ID_AGREG, 7,8)])

#### on récupère les coûts pour les bâtiments de type nr 
bibli_couts_comp[substring(ID_AGREG, 5,6) == "60" & EXIGENCE != "Maximale" & EXIGENCE != "Etat initial" & COUT == 0 & GAIN == 0, ]

bibli_couts_comp[substring(ID_AGREG, 5,6) == "60" & EXIGENCE != "Maximale" & EXIGENCE != "Etat initial" & COUT == 0 & GAIN == 0, 
                 list(mean(COUT_old),mean(GAIN_old)), by="GESTE"]



bibli_couts_comp[substring(ID_AGREG, 5,6) == "60" & EXIGENCE != "Maximale" & EXIGENCE != "Etat initial" & COUT == 0 & GAIN == 0, 
                 COUT :=COUT_old]
bibli_couts_comp[substring(ID_AGREG, 5,6) == "60" & EXIGENCE != "Maximale" & EXIGENCE != "Etat initial" & GAIN == 0, 
                 GAIN :=GAIN_old]
bibli_couts_comp[substring(ID_AGREG, 5,6) == "60" & EXIGENCE != "Maximale" & EXIGENCE != "Etat initial" & GAIN == 0, 
                 CEE :=CEE_old]

#### données présentes avant et absentes dans la nouvelle base
bibli_couts_comp[ EXIGENCE != "Maximale" & EXIGENCE != "Etat initial" & COUT == 0 & GAIN == 0 & GAIN_old!=0]
unique(bibli_couts_comp[ EXIGENCE != "Maximale" & EXIGENCE != "Etat initial" & COUT == 0 & GAIN == 0 & GAIN_old!=0, substring(ID_AGREG, 7,8)])


##### absence car on a supprimé le geste ensemble mod car ni toiture ni plancher modifié
bibli_couts_comp[ EXIGENCE != "Maximale" & EXIGENCE != "Etat initial" & COUT == 0 & GAIN == 0 & GAIN_old!=0 &  substring(ID_AGREG, 7,8) %in% c("01","02","03"), ID_AGREG]
ID_NA = bibli_couts_comp[ EXIGENCE != "Maximale" & EXIGENCE != "Etat initial" & COUT == 0 & GAIN == 0 & GAIN_old!=0 &  substring(ID_AGREG, 7,8) %in% c("01","02","03"), ID_AGREG]
data_couts_gestes[ID_AGREG %in% ID_NA & GESTE == "ENS_MOD"]
data_couts_gestes_save[ID_AGREG %in% ID_NA & GESTE != "Etat initial" & GESTE == "ENS_MOD", ]
bibli_couts[ID_AGREG %in% ID_NA & GESTE == "ENSMOD", ]

##### périodes non présentes 
bibli_couts_comp[ EXIGENCE != "Maximale" & EXIGENCE != "Etat initial" & COUT == 0 & GAIN == 0 & GAIN_old!=0 &  substring(ID_AGREG, 7,8) %in% c("01","02","03") == F, 
                  ]

##### coûts non nuls dans la version précédente et nuls dans la nouvelle version

bibli_couts_comp[COUT == 0 & COUT_old!=0,GESTE]
bibli_couts_comp[COUT < COUT_old & COUT != 0  ,]
bibli_couts_comp[COUT > COUT_old,]

#### 

bibli_couts = bibli_couts_comp[, list(ID_AGREG,EXIGENCE,GESTE,GAIN,COUT,CEE)]

####### traitement CEE 

#### on ajoute les valeurs de l'ancienne bibli 

bibli_couts = merge(bibli_couts,bibli_old[, list(CEEold=CEE, ID_AGREG, GESTE)], by=c("ID_AGREG", "GESTE"), all.x=T)

#### gain nul : CEE nul
bibli_couts[GAIN == 0, CEE :=0]
#bibli_couts[GAIN == 0, CEE_ADEME :=0]


#### CEE non présents dans la base ancienne

#### on regarde si c'est juste lié à la période de construction absente dans l'ancienne base pour un type de bâtiment
#### dans ce cas on impute les CEE des autres périodes 

ID_sansperiode_list = bibli_couts[is.na(CEEold)  & EXIGENCE != "Maximale", unique(substring(ID_AGREG, 1,6))]

bibli_couts[, ID_sansperiode := substring(ID_AGREG, 1,6)]

for(ID_temp in ID_sansperiode_list){
  

  for(GESTE_tmp in bibli_couts[substring(ID_AGREG, 1,6) == ID_temp  & EXIGENCE != "Maximale" & is.na(CEEold)
                               & GAIN != 0,unique(GESTE)]){
    
    bibli_couts[substring(ID_AGREG, 1,6) == ID_temp  & EXIGENCE != "Maximale" & is.na(CEEold) & GAIN != 0 
                & GESTE == GESTE_tmp, 
                CEE_imput := 1]
    
  bibli_couts[substring(ID_AGREG, 1,6) == ID_temp  & EXIGENCE != "Maximale" & is.na(CEEold) & GAIN != 0 
              & GESTE == GESTE_tmp, 
              CEEold := 
  bibli_couts[substring(ID_AGREG, 1,6) == ID_temp  & EXIGENCE != "Maximale" & !is.na(CEEold) & GAIN != 0 
              & GESTE == GESTE_tmp, 
              mean(CEEold)]]
 
    
  }
}

toto= bibli_couts[CEE_imput == 1,]

bibli_couts[is.na(CEEold)  & EXIGENCE != "Maximale" & GAIN!=0,]
bibli_couts[is.na(CEE)  & EXIGENCE != "Maximale" & GAIN!=0,]
bibli_couts[CEE==0  & EXIGENCE != "Maximale" & GAIN!=0 & GESTE!="GTB",CEE:=CEEold]
bibli_couts[CEE==0  & EXIGENCE != "Maximale" & GAIN!=0 & GESTE!="GTB",ID_AGREG]



bibli_couts_final = bibli_couts[, list(ID_AGREG,EXIGENCE,GESTE,GAIN,COUT,CEE,CEEold)]

##### ajouts des autres période : même gains et coûts que la période la plus récente


periode_simple_simu = c("04","05","06","07","08")
periode_detail_simu = c("19","20","21","22","23")


for( i in 1:length(periode_detail_simu)){
  
  bibli_couts_periodetmp = bibli_couts_final[substring(ID_AGREG,9,10) == "01"]
  bibli_couts_periodetmp[,ID_AGREG := paste0(substring(ID_AGREG, 1,6), 
                                           periode_detail_simu[i],periode_simple_simu[i]) ,]
  
  bibli_couts_periodetmp  = bibli_couts_periodetmp[!duplicated(paste0(ID_AGREG,GESTE))]
  
  bibli_couts_final = rbind(bibli_couts_final,bibli_couts_periodetmp)
  
  }




bibli_couts_final[ ,GESTE := factor(GESTE,levels = c("ENSMAX","ENSBBC","ENSMOD","FEN_MURMAX","FEN_MURBBC","FEN_MURMOD",
                                                     "FENMAX","FENBBC","FENMOD","GTB"))]

##### stats apres traitement 

data_couts_gestes[COUT_ENS_MOY_M2!=0, list(sum(COUT_ENS_MOY_M2*SURFACE_CHAUFFEE)/sum(SURFACE_CHAUFFEE),
                                           sum(BESOIN*GAIN_ENS)/sum(BESOIN)),
                  by=c("GESTE")]
data_couts_gestes[COUT_ENS_MOY_M2!=0,  list(sum(COUT_ENS_MOY_M2*SURFACE_CHAUFFEE)/sum(SURFACE_CHAUFFEE),
                                            sum(BESOIN*GAIN_ENS)/sum(BESOIN)), 
                  by=c("GESTE", "BRANCHE")][order(BRANCHE)]

data_couts_gestes[COUT_ENS_MOY_M2!=0, list(mean(COUT_ENS_MOY_M2),
                                           mean(GAIN_ENS)),
                  by=c("GESTE")]

data_couts_gestes[COUT_FEN_MOY_M2!=0, list(sum(COUT_FEN_MOY_M2*SURFACE_CHAUFFEE)/sum(SURFACE_CHAUFFEE),
                                           sum(BESOIN*GAIN_FEN)/sum(BESOIN)),
                  by=c("GESTE")]
data_couts_gestes[COUT_FEN_MOY_M2!=0, list(sum(COUT_FEN_MOY_M2*SURFACE_CHAUFFEE)/sum(SURFACE_CHAUFFEE),
                                           sum(BESOIN*GAIN_FEN)/sum(BESOIN)),
                  by=c("GESTE", "BRANCHE")][order(BRANCHE)]




data_couts_gestes[COUT_FENMUR_MOY_M2!=0, list(sum(COUT_FENMUR_MOY_M2*SURFACE_CHAUFFEE)/sum(SURFACE_CHAUFFEE),
                                              sum(BESOIN*GAIN_FENMUR)/sum(BESOIN)),
                  by=c("GESTE")]

data_couts_gestes[COUT_FENMUR_MOY_M2!=0, list(sum(COUT_FENMUR_MOY_M2*SURFACE_CHAUFFEE)/sum(SURFACE_CHAUFFEE),
                                           sum(BESOIN*GAIN_FENMUR)/sum(BESOIN)),
                  by=c("GESTE", "BRANCHE")][order(BRANCHE)]


data_couts_gestes[GAIN_FEN > 0  & COUT_FEN_MOY_M2!=0 , list(sum(COUT_FEN_MOY_M2*SURFACE_CHAUFFEE)/sum(SURFACE_CHAUFFEE),
                                           sum(BESOIN*GAIN_FEN)/sum(BESOIN), 
                                           mean(COUT_FEN_MOY_M2), min(COUT_FEN_MOY_M2), max(COUT_FEN_MOY_M2), 
                                           mean(GAIN_FEN), min(GAIN_FEN), max(GAIN_FEN)),
                  by=c("GESTE")]

data_couts_gestes[GAIN_FENMUR  > 0  & COUT_MUR_MOY_M2!=0 , list(sum(COUT_MUR_MOY_M2*SURFACE_CHAUFFEE)/sum(SURFACE_CHAUFFEE),
                                                            sum(BESOIN*(GAIN_FENMUR-GAIN_FEN))/sum(BESOIN), 
                                                            mean(COUT_MUR_MOY_M2), min(COUT_MUR_MOY_M2), max(COUT_MUR_MOY_M2), 
                                                            mean(GAIN_FENMUR-GAIN_FEN), min(GAIN_FENMUR-GAIN_FEN), max(GAIN_FENMUR-GAIN_FEN)),
                  by=c("GESTE")]



bibli_couts_final[ GAIN >0 & EXIGENCE != "Maximale",list(GAIN = mean(GAIN), COUT = mean( COUT)), by=c("GESTE")][order(GESTE)]

plot_COUT = bibli_couts_final
plot_COUT[, BRANCHE := substring(ID_AGREG,1,2)]
plot_COUT = plot_COUT[ GAIN >0 & EXIGENCE != "Maximale", 
                     list(GAIN = mean(GAIN), COUT = mean( COUT)), by=c("GESTE","BRANCHE")][order(GESTE)]

ggplot(plot_COUT[GESTE!="GTB"]) + geom_point(aes(GAIN,COUT, color = BRANCHE)) + geom_line(aes(GAIN,COUT, color = BRANCHE))


##### niveau de subvention et forfaits moyens

bibli_couts_final[EXIGENCE != "Maximale" &  CEE >0,  mean(CEE), by="GESTE"][order(GESTE)]

bibli_couts_final[EXIGENCE != "Maximale" &  CEE >0,  mean(CEE*0.015/COUT), by="GESTE"][order(GESTE)]

####################################################
##### Ecriture de la bibli pour le modèle
####################################################*

bibli_couts_final[duplicated(paste0(ID_AGREG,GESTE))]

write.table( bibli_couts_final[,list(ID_AGREG,EXIGENCE,GESTE,GAIN,COUT,CEE)], 
             "../docs_completementaires_EN/Bibli_geste_bati_all.csv", sep=";", dec=",", quote = T, row.names = F)
write.table( bibli_couts_final[EXIGENCE != "Maximale",list(ID_AGREG,EXIGENCE,GESTE,GAIN,COUT,CEE)], 
             "../docs_completementaires_EN/Bibli_geste_bati_pour modèle.csv", sep=";", dec=",", quote = T, row.names = F)


write.table( bibli_couts_final[,list(ID_AGREG,EXIGENCE,GESTE,GAIN,COUT,CEE=CEEold)],
             "../docs_completementaires_EN/Bibli_geste_bati_all_CEE_ADEME.csv", sep=";", dec=".", quote = T, row.names = F)
write.table( bibli_couts_final[EXIGENCE != "Maximale",list(ID_AGREG,EXIGENCE,GESTE,GAIN,COUT,CEE=CEEold)], 
             "../docs_completementaires_EN/Bibli_geste_bati_new_CEE_ADEME.csv", sep=";", dec=".", quote = T, row.names = F)


write.table(data_couts_gestes, "../docs_completementaires_EN/calculs_gains_gestes_results.csv", 
            sep=";", dec=".", quote = T, row.names = F)

#### verif si on a des coûts pour tous les segments existant dans le modèle

besoins_chauffage_init = fread("../tertiaire/parametrage modèle/Besoin_chauffage_init.csv", dec = ",", 
                               colClasses =
                                 list("character"=c("ID_segment","ID_AGREG2", "ID_AGREG", "ID_AGREG_RDT_COUT", 
                                                    "ID","ID_BRANCHE","ID_BAT_TYPE","ID_PRODUCTION_CHAUD","ID_ENERGIE")))

besoins_chauffage_init[,ID_AGREG := paste0(substring(ID, 1,6), substring(ID, 9,12))] 
besoins_chauffage_init[,ID_AGREG_calibCINT := paste0(substring(ID, 1,8), substring(ID, 9,10))] 

besoins_chauffage_init[ID_AGREG %in% 
                         bibli_couts_final$ID_AGREG ==T] 


#####  listes des bâtiments avec aucun geste 
liste_ID_parc = besoins_chauffage_init[,list(ID_AGREG  ,
                                             `SURFACES 2009`,BESOIN_U, ID_BRANCHE, ID_BAT_TYPE )] 


liste_ID_parc[ID_AGREG %in% bibli_couts$ID_AGREG == F]
liste_ID_parc[ID_AGREG %in% bibli_couts[COUT !=0 & EXIGENCE != "Maximale"]$ID_AGREG == F]
liste_ID_parc[ID_AGREG %in% bibli_couts[COUT !=0 & EXIGENCE != "Maximale"]$ID_AGREG == F, unique(ID_BAT_TYPE)]


##### stats sur un type de bâtiment
unique(data_couts_gestes$BAT_TYPE)
plateau_marge_ID = data_couts_gestes[BAT_TYPE  ==  "plateau large bureau paysagés",ID_AGREG]
bibli_couts_final[ID_AGREG %in% plateau_marge_ID ][ GAIN >0 & EXIGENCE != "Maximale",list(GAIN = mean(GAIN), COUT = mean( COUT)), by=c("GESTE")][order(GESTE)]

college_ID = data_couts_gestes[BAT_TYPE  ==   "Collège",ID_AGREG]
bibli_couts_final[ID_AGREG %in% college_ID ][ GAIN >0 & EXIGENCE != "Maximale",list(GAIN = mean(GAIN), COUT = mean( COUT)), by=c("GESTE")][order(GESTE)]

   
supermarche_ID = data_couts_gestes[BAT_TYPE  ==   "Supermarché" ,ID_AGREG]
bibli_couts_final[ID_AGREG %in% supermarche_ID  ][ GAIN >0 & EXIGENCE != "Maximale",list(GAIN = mean(GAIN), COUT = mean( COUT)), by=c("GESTE")][order(GESTE)]
