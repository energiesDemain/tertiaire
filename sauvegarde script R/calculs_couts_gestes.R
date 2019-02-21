require(data.table)
require(ggplot2)

data_couts_gestes = fread("../docs_completementaires_EN/tertiaire_cgdd_brut_V2.csv", sep = ";", dec=",")
repart_gain = fread("../docs_completementaires_EN/Repart_gain_gestes.csv", sep=";", dec=",")

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

## renomme geste

data_couts_gestes[, GESTE := factor(SCENARIO, levels = c("Etat initial","Modeste bati","BBC renovation","Maximal"), 
                                    labels = c("Etat initial", "ENS_MOD","ENS_BBC","ENS_MAX"))]

data_couts_gestes[BAT_TYPE=="Grand Hotel avec restaurant Luxe"] 
summary(data_couts_gestes)

## aggregation sur toutes les énergies 
data_couts_gestes  = melt(data_couts_gestes, id.vars = c("SCENARIO","GESTE","BRANCHE","SS_BRANCHE","BAT_TYPE",
                                                          "PERIODE_DETAIL", "PERIODE_SIMPLE","ENERGIE"))


data_couts_gestes = dcast.data.table(data_couts_gestes, 
                                     BRANCHE+SS_BRANCHE+BAT_TYPE+PERIODE_DETAIL+PERIODE_SIMPLE +  GESTE~ variable, 
                                     fun.aggregate = sum, value.var = "value") 


#### on enlève les segments non chauffés

data_couts_gestes = data_couts_gestes[SURFACE_CHAUFFEE!=0 ]

#### calcul gain total du geste
data_couts_gestes = merge(data_couts_gestes, 
                          data_couts_gestes[GESTE == "Etat initial",list(BESOIN_INIT = BESOIN),  
                                            by=c("BRANCHE","SS_BRANCHE","BAT_TYPE",
                                                 "PERIODE_DETAIL", "PERIODE_SIMPLE")], 
                          by=c("BRANCHE","SS_BRANCHE","BAT_TYPE",
                               "PERIODE_DETAIL", "PERIODE_SIMPLE"))

data_couts_gestes[, GAIN_TOT := 1-BESOIN/BESOIN_INIT]

#### calcul couts moyens par m2 de shon ensemble du geste et par paroi
data_couts_gestes[,COUT_PLAN_MOY_M2 :=  (COUT_PLAN_MIN +  COUT_PLAN_MAX)/2/SURFACE_CHAUFFEE]
data_couts_gestes[,COUT_FEN_MOY_M2 :=  (COUT_FEN_MIN +  COUT_FEN_MAX)/2/SURFACE_CHAUFFEE]
data_couts_gestes[,COUT_MUR_MOY_M2 :=  (COUT_MUR_MIN +  COUT_MUR_MAX)/2/SURFACE_CHAUFFEE]
data_couts_gestes[,COUT_TOIT_MOY_M2 :=  (COUT_TOIT_MIN +  COUT_TOIT_MAX)/2/SURFACE_CHAUFFEE]
data_couts_gestes[,COUT_ENS_MOY_M2 :=  COUT_PLAN_MOY_M2 + COUT_FEN_MOY_M2 + COUT_MUR_MOY_M2 + COUT_TOIT_MOY_M2]
data_couts_gestes[,COUT_FENMUR_MOY_M2 :=  COUT_FEN_MOY_M2 + COUT_MUR_MOY_M2]

summary(data_couts_gestes)

#### modif en mettant le coût max
data_couts_gestes[,COUT_PLAN_MOY_M2 :=  COUT_PLAN_MAX/SURFACE_CHAUFFEE]
data_couts_gestes[,COUT_FEN_MOY_M2 := COUT_FEN_MAX/SURFACE_CHAUFFEE]
data_couts_gestes[,COUT_MUR_MOY_M2 :=   COUT_MUR_MAX/SURFACE_CHAUFFEE]
data_couts_gestes[,COUT_TOIT_MOY_M2 :=  COUT_TOIT_MAX/SURFACE_CHAUFFEE]
data_couts_gestes[,COUT_ENS_MOY_M2 :=  COUT_PLAN_MOY_M2 + COUT_FEN_MOY_M2 + COUT_MUR_MOY_M2 + COUT_TOIT_MOY_M2]
data_couts_gestes[,COUT_FENMUR_MOY_M2 :=  COUT_FEN_MOY_M2 + COUT_MUR_MOY_M2]


summary(data_couts_gestes)

#### verif gain nul etat initial
data_couts_gestes[GESTE == "Etat initial" & GAIN_TOT != 0]

#### gain nul et coûts non nuls
data_couts_gestes[GESTE != "Etat initial" & GAIN_TOT == 0 & COUT_ENS_MOY_M2 !=0]

#### gain inférieur à zéro 

data_couts_gestes[GAIN_TOT <0]
data_couts_gestes[GAIN_TOT <0 & COUT_ENS_MOY_M2 == 0, GAIN_TOT := 0]

#### coûts nuls et gains non nuls
data_couts_gestes[ COUT_ENS_MOY_M2 == 0 & GAIN_TOT!=0, ]
data_couts_gestes[ COUT_ENS_MOY_M2 == 0, GAIN_TOT := 0]


#### gain très faible 
data_couts_gestes[GAIN_TOT < 0.05& GAIN_TOT !=0]

#### stats par geste 
data_couts_gestes[GAIN_TOT !=0 ,list(GAIN = mean(GAIN_TOT), COUT_ENS_MOY_M2 = mean( COUT_ENS_MOY_M2)), by=c("GESTE")]
data_couts_gestes[GAIN_TOT !=0 ,list(GAIN = mean(GAIN_TOT), COUT_ENS_MOY_M2 = mean( COUT_ENS_MOY_M2)), by=c("GESTE", "BRANCHE")]


#### repartitions des gains par paroi 
summary(repart_gain)
data_couts_gestes = merge(data_couts_gestes, repart_gain, by=c("BRANCHE","BAT_TYPE", "PERIODE_SIMPLE"))

data_couts_gestes[is.na(REPART_FEN)]
data_couts_gestes[is.na(REPART_FENMUR)]
data_couts_gestes[is.na(REPART_ENS)]
data_couts_gestes[is.na(REPART_ENS)]

### Répartition reste gains entre plancher et toiture
partTOIT = 2/3
allvars = c("REPART_FEN","REPART_MUR", "REPART_PLAN", "REPART_TOIT", 
            "REPART_PLANTOIT","REPART_FENMUR","REPART_ENS")

###  
data_couts_gestes[, REPART_MUR := REPART_FENMUR - REPART_FEN]
data_couts_gestes[, REPART_PLANTOIT := REPART_ENS - REPART_FENMUR]
data_couts_gestes[, REPART_PLAN := REPART_PLANTOIT*(1-partTOIT)]
data_couts_gestes[, REPART_TOIT := REPART_PLANTOIT*(partTOIT)]

### coût nul pour une paroi, gain nul
data_couts_gestes[COUT_MUR_MOY_M2 == 0, REPART_MUR := 0]
data_couts_gestes[COUT_FEN_MOY_M2 == 0, REPART_FEN := 0]
data_couts_gestes[COUT_PLAN_MOY_M2 == 0 , REPART_PLAN := 0]
data_couts_gestes[COUT_TOIT_MOY_M2 == 0, REPART_TOIT := 0]
data_couts_gestes[COUT_TOIT_MOY_M2 == 0 & COUT_PLAN_MOY_M2 == 0, REPART_PLANTOIT := 0]
data_couts_gestes[COUT_FEN_MOY_M2 == 0 & COUT_MUR_MOY_M2 == 0, REPART_FENMUR := 0]
data_couts_gestes[COUT_FEN_MOY_M2 == 0 & COUT_MUR_MOY_M2 == 0 & COUT_TOIT_MOY_M2 == 0 & COUT_PLAN_MOY_M2 == 0,
                  REPART_ENS := 0]

### cas une seule paroi est changée, on impute tout le gain au geste ensemble qui est le seul geste restant

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
#### cas où pas de toiture ou pas de plancher mais l'autre oui, on donne la partie toiture + plancher aux toires ou aux plachers

data_couts_gestes[GAIN_TOT != 0 &  COUT_TOIT_MOY_M2 ==0 & COUT_PLAN_MOY_M2 != 0, REPART_PLAN := REPART_PLANTOIT]
data_couts_gestes[GAIN_TOT != 0 &  COUT_TOIT_MOY_M2 ==0 & COUT_PLAN_MOY_M2 != 0, REPART_TOIT := 0]
data_couts_gestes[GAIN_TOT != 0 &  COUT_TOIT_MOY_M2 !=0 & COUT_PLAN_MOY_M2 == 0, REPART_TOIT := REPART_PLANTOIT]
data_couts_gestes[GAIN_TOT != 0 &  COUT_TOIT_MOY_M2 !=0 & COUT_PLAN_MOY_M2 == 0, REPART_PLAN := 0]

#### verif
data_couts_gestes[COUT_MUR_MOY_M2 == 0 & REPART_MUR != 0]
data_couts_gestes[COUT_FEN_MOY_M2 == 0 & REPART_FEN != 0]
data_couts_gestes[COUT_PLAN_MOY_M2 == 0 & REPART_PLAN != 0]
data_couts_gestes[COUT_TOIT_MOY_M2 == 0 & REPART_TOIT != 0]

data_couts_gestes[GAIN_TOT != 0 & COUT_ENS_MOY_M2 != 0 & (REPART_FEN + REPART_MUR + REPART_TOIT+ REPART_PLAN !=1),]

data_couts_gestes[GAIN_TOT != 0 & COUT_ENS_MOY_M2 != 0 & REPART_FENMUR != 0 &  (REPART_FEN + REPART_MUR != REPART_FENMUR),]

data_couts_gestes[GAIN_TOT != 0 & COUT_ENS_MOY_M2 != 0 & (REPART_TOIT + REPART_PLAN -REPART_PLANTOIT >0.01),]

data_couts_gestes[GAIN_TOT != 0 & COUT_ENS_MOY_M2 != 0 & REPART_FEN == 1 & REPART_ENS == 1,]
data_couts_gestes[GAIN_TOT != 0 & COUT_ENS_MOY_M2 != 0 & REPART_FENMUR == 1 & REPART_ENS == 1,]
data_couts_gestes[GAIN_TOT != 0 & COUT_ENS_MOY_M2 != 0 &  REPART_ENS == 0 & (REPART_FEN != 1 | REPART_FENMUR != 1),]
data_couts_gestes[GAIN_TOT != 0 & COUT_ENS_MOY_M2 != 0 &  REPART_ENS == 0 & REPART_FEN != 1 & REPART_FENMUR != 1,]

#### calculs gain par parois et par gestes
data_couts_gestes[,GAIN_FEN := GAIN_TOT*REPART_FEN]
data_couts_gestes[,GAIN_FENMUR := GAIN_TOT*REPART_FENMUR]
data_couts_gestes[,GAIN_ENS := GAIN_TOT*REPART_ENS]

data_couts_gestes[GAIN_ENS != GAIN_TOT & GAIN_ENS!=0]
data_couts_gestes[COUT_FEN_MOY_M2 != 0 & GAIN_FEN== 0  ]
data_couts_gestes[COUT_FENMUR_MOY_M2 != 0 & COUT_MUR_MOY_M2 != 0  & GAIN_FENMUR== 0  ]


#### On vire les gestes avec gain nul
data_couts_gestes[ GAIN_TOT == 0 , COUT_FEN_MOY_M2 := 0 ]
data_couts_gestes[ GAIN_TOT == 0 , COUT_FENMUR_MOY_M2 := 0 ]
data_couts_gestes[ GAIN_FEN == 0 , COUT_FEN_MOY_M2 := 0 ]
data_couts_gestes[ GAIN_FENMUR == 0 , COUT_FENMUR_MOY_M2 := 0 ]
data_couts_gestes[ GAIN_ENS == 0 , COUT_ENS_MOY_M2 := 0 ]

#### création table finale input modèle tertiaire

data_couts_gestes = merge(data_couts_gestes, COD_BRANCHE, by="BRANCHE")
data_couts_gestes = merge(data_couts_gestes, COD_SS_BRANCHE, by="SS_BRANCHE")
data_couts_gestes = merge(data_couts_gestes, COD_BAT_TYPE, by="BAT_TYPE")
data_couts_gestes = merge(data_couts_gestes, COD_PERIODE_DETAIL, by="PERIODE_DETAIL")
data_couts_gestes = merge(data_couts_gestes, COD_PERIODE_SIMPLE, by="PERIODE_SIMPLE")

data_couts_gestes[,ID_AGREG := paste0(COD_BRANCHE, COD_SS_BRANCHE, COD_BAT_TYPE,COD_PERIODE_DETAIL, COD_PERIODE_SIMPLE)]
data_couts_gestes[GESTE == "ENS_MOD",EXIGENCE := "RT par element" ]
data_couts_gestes[GESTE == "ENS_MOD",EXIGENCE := "RT par element" ]
data_couts_gestes[GESTE == "ENS_BBC",EXIGENCE := "BBC renovation" ]
data_couts_gestes[GESTE == "ENS_MAX",EXIGENCE := "Maximale" ]




# ##### modif CEE à partir de ADEME gisement
# data_couts_gestes[,CEE_ADEME := 0]
# data_couts_gestes[COUT_PLAN_MOY_M2 > 0, CEE_PLAN :=  296147/80]
# data_couts_gestes[COUT_FEN_MOY_M2 > 0, CEE_FEN := 31566/80]
# data_couts_gestes[COUT_MUR_MOY_M2 > 0, CEE_MUR :=  204441/80]
# data_couts_gestes[COUT_TOIT_MOY_M2 > 0, CEE_TOIT := 150770/80]

##### modif CEE à partir des fiches  

CEE_recalc = fread("../AME_AMS_dataDGEC/CEE/bibli_chauffage_CEE.csv",dec = ",")
data_couts_gestes = merge(data_couts_gestes,CEE_recalc[,list(BRANCHE, SS_BRANCHE, BAT_TYPE,CEE_TOIT,CEE_PLAN, CEE_FEN,CEE_MUR)], 
                          by=c("BRANCHE","SS_BRANCHE","BAT_TYPE"), all.x = T)


data_couts_gestes[COUT_PLAN_MOY_M2 == 0, CEE_PLAN :=  0]
data_couts_gestes[COUT_FEN_MOY_M2 == 0, CEE_FEN := 0]
data_couts_gestes[COUT_MUR_MOY_M2 == 0, CEE_MUR :=  0]
data_couts_gestes[COUT_TOIT_MOY_M2== 0, CEE_TOIT := 0]

data_couts_gestes[COUT_PLAN_MOY_M2 == 0, CEE_PLAN :=  0]
data_couts_gestes[COUT_FEN_MOY_M2 == 0, CEE_FEN := 0]
data_couts_gestes[COUT_MUR_MOY_M2 == 0, CEE_MUR :=  0]
data_couts_gestes[COUT_TOIT_MOY_M2== 0, CEE_TOIT := 0]


data_couts_gestes[,CEE_FEN := CEE_FEN]
data_couts_gestes[,CEE_FENMUR :=CEE_FEN +CEE_MUR]
data_couts_gestes[,CEE_ENS := CEE_FEN +CEE_MUR +  CEE_PLAN + CEE_TOIT]

summary(data_couts_gestes)


##### 
bibli_couts =  data_couts_gestes[,list(ID_AGREG,EXIGENCE,
                        COUT_FEN = COUT_FEN_MOY_M2 ,COUT_FENMUR = COUT_FENMUR_MOY_M2, COUT_ENS = COUT_ENS_MOY_M2,
                        GAIN_FEN,GAIN_FENMUR, GAIN_ENS,CEE_FEN,CEE_FENMUR, CEE_ENS )]

bibli_couts = melt(bibli_couts[!is.na(EXIGENCE)],id.vars = c("ID_AGREG","EXIGENCE"))
bibli_couts[,GESTE := as.character(lapply(strsplit(as.character(variable), split = "_"),"[", 2))]
bibli_couts[,variable := as.character(lapply(strsplit(as.character(variable), split = "_"),"[", 1))]

data_couts_gestes[ID_AGREG == "0409031904"]

#### modifie le nom des gestes pour correspondre au modèle

bibli_couts[GESTE == "FENMUR",GESTE :="FEN_MUR"]

bibli_couts[EXIGENCE == "RT par element",GESTE := paste(GESTE,"MOD",sep="")]
bibli_couts[EXIGENCE == "BBC renovation",GESTE := paste(GESTE,"BBC",sep="")]
bibli_couts[EXIGENCE == "Maximale",GESTE := paste(GESTE,"MAX",sep="")]

bibli_couts = dcast.data.table(bibli_couts , ID_AGREG + EXIGENCE + GESTE  ~ variable)

setnames(bibli_couts,"CEE","CEE_ADEME")

bibli_couts[is.na(COUT)]
##### ajoute GTB
GTB = data.table(ID_AGREG = unique(bibli_couts$ID_AGREG),EXIGENCE = "GTB",
                 GESTE = "GTB","COUT" = 20, GAIN=0.15, CEE_ADEME = 0)

bibli_couts = rbind(bibli_couts, GTB)


##### comparaison avec l'ancienne bibli
bibli_old =  fread("table_param_origine/Bibli_geste_bati.csv", 
                   colClasses = list("character" = c("ID_AGREG","EXIGENCE","GESTE")))

bibli_couts_comp = merge(bibli_couts, bibli_old[,list(CEE, ID_AGREG, GESTE, GAIN_old = GAIN, COUT_old = COUT)], by=c("ID_AGREG", "GESTE"), all.x=T)


bibli_couts_comp[is.na(COUT)]

#### on récupère les coûts pour les bâtiments de type nr 
bibli_couts_comp[substring(ID_AGREG, 5,6) == "60" & EXIGENCE != "Maximale" & COUT == 0 & GAIN == 0, 
                 COUT :=COUT_old]
bibli_couts_comp[substring(ID_AGREG, 5,6) == "60" & EXIGENCE != "Maximale" & GAIN == 0, 
                 GAIN :=GAIN_old]


##### coûts non nuls dans la version précédente et nuls dans la nouvelle version

bibli_couts_comp[COUT == 0 & COUT_old!=0,]


bibli_couts = bibli_couts_comp[, list(ID_AGREG,EXIGENCE,GESTE,GAIN,COUT,CEE_ADEME)]

####### traitement CEE 

#### on ajoute les valeurs de l'ancienne bibli

bibli_couts = merge(bibli_couts,bibli_old[, list(CEE, ID_AGREG, GESTE)], by=c("ID_AGREG", "GESTE"), all.x=T)

#### gain nul : CEE nul
bibli_couts[GAIN == 0, CEE :=0]
bibli_couts[GAIN == 0, CEE_ADEME :=0]


#### CEE non présents dans la base ancienne

#### on regarde si c'est juste lié à la péiode de construction absente dans l'ancienne base pour un type de bâtiment
#### dans ce cas on impute les CEE des autres périodes 

ID_sansperiode_list = bibli_couts[is.na(CEE)  & EXIGENCE != "Maximale", unique(substring(ID_AGREG, 1,6))]

bibli_couts[, ID_sansperiode := substring(ID_AGREG, 1,6)]

for(ID_temp in ID_sansperiode_list){
  

  for(GESTE_tmp in bibli_couts[substring(ID_AGREG, 1,6) == ID_temp  & EXIGENCE != "Maximale" & is.na(CEE)
                               & GAIN != 0,unique(GESTE)]){
    
    bibli_couts[substring(ID_AGREG, 1,6) == ID_temp  & EXIGENCE != "Maximale" & is.na(CEE) & GAIN != 0 
                & GESTE == GESTE_tmp, 
                CEE_imput := 1]
    
  bibli_couts[substring(ID_AGREG, 1,6) == ID_temp  & EXIGENCE != "Maximale" & is.na(CEE) & GAIN != 0 
              & GESTE == GESTE_tmp, 
              CEE := 
  bibli_couts[substring(ID_AGREG, 1,6) == ID_temp  & EXIGENCE != "Maximale" & !is.na(CEE) & GAIN != 0 
              & GESTE == GESTE_tmp, 
              mean(CEE)]]
 
    
  }
}

toto= bibli_couts[CEE_imput == 1,]

bibli_couts[is.na(CEE)  & EXIGENCE != "Maximale",]


bibli_couts_final = bibli_couts[, list(ID_AGREG,EXIGENCE,GESTE,GAIN,COUT,CEE, CEE_ADEME)]

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
bibli_couts_final[ ID_AGREG == "0409031904"]


##### 
bibli_couts_final[GESTE == "ENSMOD", GAIN] 
bibli_couts_final[ID_AGREG == "0101420701"]

data_couts_gestes[COUT_ENS_MOY_M2!=0, sum(COUT_ENS_MOY_M2*SURFACE_CHAUFFEE)/sum(SURFACE_CHAUFFEE), 
                  by=c("GESTE", "BRANCHE")][order(BRANCHE)]
data_couts_gestes[COUT_FEN_MOY_M2!=0, list(sum(COUT_FEN_MOY_M2*SURFACE_CHAUFFEE)/sum(SURFACE_CHAUFFEE),
                                           sum(BESOIN*GAIN_FEN)/sum(BESOIN)),
                  by=c("GESTE", "BRANCHE")][order(BRANCHE)]
data_couts_gestes[COUT_FENMUR_MOY_M2!=0, list(sum(COUT_FENMUR_MOY_M2*SURFACE_CHAUFFEE)/sum(SURFACE_CHAUFFEE),
                                           sum(BESOIN*GAIN_FENMUR)/sum(BESOIN)),
                  by=c("GESTE", "BRANCHE")][order(BRANCHE)]


##### stats après traitement
bibli_couts_final[ GAIN >0 & EXIGENCE != "Maximale",list(GAIN = mean(GAIN), COUT = mean( COUT)), by=c("GESTE")]

bibli_couts_final[substring(ID_AGREG,1,2) == "01" & GAIN >0 & EXIGENCE != "Maximale", 
                  list(GAIN = mean(GAIN), COUT = mean( COUT)), by=c("GESTE")]



##### niveau de subvention et forfaits moyens

bibli_couts_final[EXIGENCE != "Maximale" &  CEE >0,  mean(CEE), by="GESTE"]

bibli_couts_final[EXIGENCE != "Maximale" &  CEE >0,  mean(CEE*0.015/COUT), by="GESTE"]

##### Ecriture de la bibli pour le modèle

bibli_couts_final[duplicated(paste0(ID_AGREG,GESTE))]
bibli_couts_final[substring(ID_AGREG,1,6) == "030766",]

bibli_couts_final[ ID_AGREG == "0409031904"]
write.table( bibli_couts_final, "../docs_completementaires_EN/Bibli_geste_bati_all.csv", sep=";", dec=".", quote = T, row.names = F)
write.table( bibli_couts_final[EXIGENCE != "Maximale"], "../docs_completementaires_EN/Bibli_geste_bati_new.csv", sep=";", dec=".", quote = T, row.names = F)

bibli_couts_final[,CEE:=CEE_ADEME]
bibli_couts_final[,CEE_ADEME :=NULL]
write.table( bibli_couts_final, "../docs_completementaires_EN/Bibli_geste_bati_all_CEE_ADEME.csv", sep=";", dec=".", quote = T, row.names = F)
write.table( bibli_couts_final[EXIGENCE != "Maximale"], "../docs_completementaires_EN/Bibli_geste_bati_new_CEE_ADEME.csv", sep=";", dec=".", quote = T, row.names = F)


write.table(data_couts_gestes, "../docs_completementaires_EN/calculs_gains_gestes_results.csv", sep=";", dec=".", quote = T, row.names = F)

#### verif si on a des coûts pour tous les segments existant dans le modèle

besoins_chauffage_init = fread("../tertiaire/parametrage modèle/Besoin_chauffage_init.csv", dec = ",", 
                               colClasses =
                                 list("character"=c("ID_segment","ID_AGREG2", "ID_AGREG", "ID_AGREG_RDT_COUT", 
                                                    "ID","ID_BRANCHE","ID_BAT_TYPE","ID_PRODUCTION_CHAUD","ID_ENERGIE")))

besoins_chauffage_init[,ID_AGREG := paste0(substring(ID, 1,6), substring(ID, 9,12))] 
besoins_chauffage_init[,ID_AGREG_calibCINT := paste0(substring(ID, 1,8), substring(ID, 9,10))] 

besoins_chauffage_init[ID_AGREG %in% 
                         bibli_couts_final$ID_AGREG ==T] 

#### calcul charges initiales 

besoins_chauffage_init[ID_ENERGIE == "01", prix_2009 := 0.045263158]
besoins_chauffage_init[ID_ENERGIE == "02", prix_2009 := 0.09407]
besoins_chauffage_init[ID_ENERGIE == "03", prix_2009 := 0.061785634]
besoins_chauffage_init[ID_ENERGIE == "04", prix_2009 := 0.0330053]
besoins_chauffage_init[ID_ENERGIE == "06", prix_2009 := 0.0614]
besoins_chauffage_init[ID_ENERGIE == "05", prix_2009 := 0]

besoins_chauffage_init[ID_ENERGIE == "01", prix_2010 := 0.050947368]
besoins_chauffage_init[ID_ENERGIE == "02", prix_2010 := 0.09847	]
besoins_chauffage_init[ID_ENERGIE == "03", prix_2010 := 0.076392929]
besoins_chauffage_init[ID_ENERGIE == "04", prix_2010 := 0.038671967]
besoins_chauffage_init[ID_ENERGIE == "06", prix_2010 := 0.0591]
besoins_chauffage_init[ID_ENERGIE == "05", prix_2010 := 0]

besoins_chauffage_init[,CHARGE_INIT := BESOIN*prix_2009/`RDT systeme`]

Charges_init = besoins_chauffage_init[,list(BESOIN = sum(BESOIN),
                                          SURFACES = sum(`SURFACES 2009`),
                                          SURFACES_recal = sum(`SURFACES 2009 recal`),
                                          CHARGE_INIT = sum(CHARGE_INIT)
                                          ), by=c("ID_AGREG_calibCINT","ID_AGREG")]

Charges_init[,CHARGE_INIT_MOY_M2 := CHARGE_INIT/SURFACES_recal]
summary(Charges_init)


#####  listes des bâtiments avec aucun geste 
liste_ID_parc = besoins_chauffage_init[,list(ID_AGREG  ,
                                             `SURFACES 2009`,BESOIN_U, ID_BRANCHE, ID_BAT_TYPE )] 


liste_ID_parc[ID_AGREG %in% bibli_couts$ID_AGREG == F]
liste_ID_parc[ID_AGREG %in% bibli_couts[COUT !=0 & EXIGENCE != "Maximale"]$ID_AGREG == F]
liste_ID_parc[ID_AGREG %in% bibli_couts[COUT !=0 & EXIGENCE != "Maximale"]$ID_AGREG == F, unique(ID_BAT_TYPE)]

##### Ecriture CINT calib

CINT_bati_Etat_ini <- Charges_init

CINT_bati_Etat_ini$GESTE = "Etat initial"
CINT_bati_Etat_ini[,COUT:= 0]
CINT_bati_Etat_ini[,GAIN := 0]
CINT_bati_Etat_ini = CINT_bati_Etat_ini[,list(ID_AGREG ,ID_AGREG_calibCINT,
                                              BESOIN,
                                              SURFACES,SURFACES_recal, CHARGE_INIT,CHARGE_INIT_MOY_M2,
                                              COD_BRANCHE = substring(ID_AGREG, 1,2),
                                              COD_SS_BRANCHE = substring(ID_AGREG, 3,4),
                                              COD_BAT_TYPE = substring(ID_AGREG, 5,6),
                                              COD_PERIODE_DETAIL = substring(ID_AGREG, 7,8), 
                                              COD_PERIODE_SIMPLE = substring(ID_AGREG, 9,10),
                                              GESTE,COUT, GAIN)]

CINT_bati= CINT_bati_Etat_ini
gestetmp = "FENMOD"


for(gestetmp in unique(bibli_couts_final[EXIGENCE != "Maximale",GESTE])){
  if(gestetmp != "Etat initial"){
  CINT_bati_tmp = Charges_init
  CINT_bati_tmp  = merge(CINT_bati_tmp, bibli_couts_final[EXIGENCE != "Maximale" & GESTE == gestetmp,list(ID_AGREG ,
                                                                             COD_BRANCHE = substring(ID_AGREG, 1,2),
                                                                             COD_SS_BRANCHE = substring(ID_AGREG, 3,4),
                                                                             COD_BAT_TYPE = substring(ID_AGREG, 5,6),
                                                                             COD_PERIODE_DETAIL = substring(ID_AGREG, 7,8), 
                                                                             COD_PERIODE_SIMPLE = substring(ID_AGREG, 9,10), 
                                                                             GESTE,COUT, GAIN)], by="ID_AGREG", all.x=T)
 
  
  CINT_bati = rbind(CINT_bati,CINT_bati_tmp)
  
  }
}


CINT_bati = merge(CINT_bati, COD_BRANCHE, by="COD_BRANCHE", all.x=T)
CINT_bati = merge(CINT_bati, COD_SS_BRANCHE, by="COD_SS_BRANCHE", all.x=T)
CINT_bati = merge(CINT_bati, COD_BAT_TYPE, by="COD_BAT_TYPE", all.x=T)
CINT_bati = merge(CINT_bati, COD_PERIODE_DETAIL, by="COD_PERIODE_DETAIL", all.x=T)
CINT_bati = merge(CINT_bati, COD_PERIODE_SIMPLE, by="COD_PERIODE_SIMPLE", all.x=T)
CINT_bati = CINT_bati[!is.na(PERIODE_DETAIL) ]
  
  
#### DV des gestes :

CINT_bati[GESTE == "ENSMOD", DV := 50]
CINT_bati[GESTE == "ENSBBC", DV := 50]
CINT_bati[GESTE == "FENBBC", DV := 20]
CINT_bati[GESTE == "FENMOD", DV := 20]
CINT_bati[GESTE == "FEN_MURMOD", DV := 20]
CINT_bati[GESTE == "FEN_MURBBC", DV := 20]
CINT_bati[GESTE == "GTB", DV := 5]
CINT_bati[GESTE == "Etat initial", DV := 1]

#### part de marché des gestes :

CINT_bati[GESTE == "ENSBBC", PM := 0.00002]
CINT_bati[GESTE == "ENSMOD", PM := 0.00004]
CINT_bati[GESTE == "FEN_MURBBC", PM := 0.0001]
CINT_bati[GESTE == "FEN_MURMOD", PM := 0.002]
CINT_bati[GESTE == "FENBBC", PM := 0.0002]
CINT_bati[GESTE == "FENMOD", PM := 0.004]

CINT_bati[GESTE == "GTB", PM := 0.0025]
CINT_bati[GESTE == "Etat initial", PM := 0]


CINT_bati[GESTE == "Etat initial",]
CINT_bati[duplicated(ID_AGREG_calibCINT)]
#### enlève les gestes à coûts nuls et les ajoute à NRF

CINT_bati[ COUT == 0 & GESTE != "Etat initial",PM:=0, by="ID_AGREG_calibCINT"]
CINT_bati[ GESTE != "Etat initial" & is.na(PM),PM:=0, by="ID_AGREG_calibCINT"]
PM_NRF_add = CINT_bati[ ,list(PM_NRF = 1-sum(PM)), by="ID_AGREG_calibCINT"]
CINT_bati = merge(CINT_bati, PM_NRF_add, by="ID_AGREG_calibCINT", all.x = T)

CINT_bati[ GESTE == "Etat initial" ,PM:= PM_NRF, by="ID_AGREG_calibCINT"]
CINT_bati[ ,sum(PM), by="ID_AGREG_calibCINT"][V1!=1]

#### COUT dinvestissement global
CINT_bati[, sum(COUT*SURFACES_recal*PM)/10^9 ]
CINT_bati[GESTE != "Etat initial", sum(SURFACES_recal*PM)/10^6 ]

#### calcul CINT

CINT_bati[substring(ID_AGREG_calibCINT,7,8) == "05", taux_actu := 0.07]
CINT_bati[substring(ID_AGREG_calibCINT,7,8) != "05", taux_actu := 0.04]

CINT_bati[, CA := (1/(1+taux_actu))*(1-(1/(1+taux_actu))^DV)/(1-(1/(1+taux_actu)))]
CINT_bati[, COUT_VARIABLE := COUT/CA + CHARGE_INIT_MOY_M2*(1-GAIN)]

#### CG ref : ne rien faire , CINT = 0
CINT_ref = 2
CINT_bati[GESTE == "Etat initial", CG:=COUT_VARIABLE + CINT_ref]

CINT_bati =  merge(CINT_bati, CINT_bati[GESTE == "Etat initial",
                                                  list(ID_AGREG_calibCINT,CG_ref = CG, PM_ref = PM )], by="ID_AGREG_calibCINT")


#### CINT
nu=8
CINT_bati[,CINT := (PM/PM_ref)^(-1/nu)*CG_ref - COUT_VARIABLE]
CINT_bati[,CG := CINT+ COUT_VARIABLE]


CINT_bati[,Part_CINT := CINT/CG]


CINT_bati[Part_CINT > 0.8, ]
CINT_bati[Part_CINT < -1 & GESTE == "ENSBBC", ]

summary(CINT_bati)
summary(CINT_bati[GESTE == "GTB"])

CINT_bati[ID_AGREG_calibCINT== "0101420111"]
CINT_bati[substring(ID_AGREG, 1,6) == "030766" & GESTE == "Etat initial"]
CINT_bati[substring(ID_AGREG, 1,6) == "030766" & GESTE == "Etat initial", CHARGE_INIT_MOY_M2*SURFACES_recal/0.05/BESOIN]
CINT_bati[substring(ID_AGREG, 1,6) == "030766" & GESTE == "GTB",]

#### effet sur parc année 2010

gestetmp = "Etat initial"
PM_geste = data.table()
for(gestetmp in unique(CINT_bati$GESTE)){
  
  PM_geste_tmp = besoins_chauffage_init
  PM_geste_tmp= merge(PM_geste_tmp, CINT_bati[GESTE == gestetmp, list(ID_AGREG_calibCINT,GESTE, COUT, GAIN, CINT, PM_th = PM, DV, CA)], 
                   by="ID_AGREG_calibCINT")
  
  PM_geste_tmp[, CG := CHARGE_INIT*prix_2010/prix_2009/`SURFACES 2009 recal`*(1-GAIN) + COUT/CA + CINT]
  PM_geste = rbind(PM_geste,PM_geste_tmp)
}

PM_geste[,sumCGnu := sum(CG^-nu), by="ID"]

PM_geste[,PM :=CG^-nu/sumCGnu]

####
PM_geste[,sum(PM), by="ID"]

PM_geste[GESTE != "Etat initial", sum(COUT*`SURFACES 2009 recal`*PM)/10^9 ]
PM_geste[GESTE != "Etat initial", sum(COUT*`SURFACES 2009 recal`*PM)/10^6, by="GESTE"]
PM_geste[GESTE != "Etat initial", sum(`SURFACES 2009 recal`*PM)/10^6 ]
PM_geste[GESTE != "Etat initial", sum(`SURFACES 2009 recal`*PM)/10^6, by="GESTE"]
PM_geste[GESTE != "Etat initial", sum(`SURFACES 2009 recal`*PM)/sum(`SURFACES 2009 recal`), by="GESTE"]


### calcul charges, gains et couts moyens par branche 
besoins_chauffage_init[ID_ENERGIE == "01", prix_2009 := 0.045263158]
besoins_chauffage_init[ID_ENERGIE == "02", prix_2009 := 0.09407]
besoins_chauffage_init[ID_ENERGIE == "03", prix_2009 := 0.061785634]
besoins_chauffage_init[ID_ENERGIE == "04", prix_2009 := 0.0330053]
besoins_chauffage_init[ID_ENERGIE == "06", prix_2009 := 0.0614]
besoins_chauffage_init[ID_ENERGIE == "05", prix_2009 := 0]

Charges_init_branche = besoins_chauffage_init[,list(ID_AGREG,
                                                   BESOIN ,`RDT systeme`,
                                                   SURFACES = `SURFACES 2009`,
                                                   prix_2009,
                                                   SURFACES_recal = `SURFACES 2009 recal`
)]
Charges_init_branche[,CHARGE_INIT := BESOIN*prix_2009/`RDT systeme`]
Charges_init_branche[, ID_BRANCHE := substring(ID_AGREG, 0,2)]



Charges_init_branche = Charges_init_branche [,list(
                                                    BESOIN = sum(BESOIN),
                                                    SURFACES = sum(SURFACES),
                                                    CHARGE_INIT = sum(CHARGE_INIT),
                                                    SURFACES_recal = sum(SURFACES_recal)), by=c("ID_BRANCHE")]

Charges_init_branche[,CHARGE_INIT_MOY_M2 := CHARGE_INIT/SURFACES_recal]

cout_geste_branche = bibli_couts_final[substring(ID_AGREG,9,10) %in% c("01","02","03") & EXIGENCE != "Maximale", ]
cout_geste_branche[, ID_BRANCHE := substring(ID_AGREG, 0,2)]

besoin_chauff_idagreg = besoins_chauffage_init[ ,list( BESOIN =sum(BESOIN),
                               SURFACES = sum(`SURFACES 2009`),
                               SURFACES_recal = sum(`SURFACES 2009 recal`)), by="ID_AGREG"]

cout_geste_branche = merge(cout_geste_branche, besoin_chauff_idagreg, by=c("ID_AGREG"), all.x  = T)

cout_geste_branche = cout_geste_branche[COUT!=0,list(COUT_MOY = sum(COUT*SURFACES_recal)/sum(SURFACES_recal),
                         GAIN_MOY = sum(GAIN*BESOIN)/sum(BESOIN)
                ), by=c("ID_BRANCHE","GESTE")]

cout_geste_branche = merge(cout_geste_branche, Charges_init_branche[, list(CHARGE_INIT = CHARGE_INIT_MOY_M2,ID_BRANCHE)], by="ID_BRANCHE")


cout_geste_branche_EI = Charges_init_branche[, list(ID_BRANCHE,GESTE ="Etat initial",CHARGE_INIT = CHARGE_INIT_MOY_M2, COUT_MOY = 0, GAIN_MOY = 0)]

cout_geste_branche = rbind(cout_geste_branche_EI,cout_geste_branche)
cout_geste_branche = merge(cout_geste_branche,COD_BRANCHE[, list(BRANCHE,ID_BRANCHE= COD_BRANCHE)],by="ID_BRANCHE")

cout_geste_branche[, ID_BRANCHE := NULL]

#### durée de vie des gestes 

cout_geste_branche[GESTE == "ENSMOD", DV := 50]
cout_geste_branche[GESTE == "ENSBBC", DV := 50]
cout_geste_branche[GESTE == "FENBBC", DV := 20]
cout_geste_branche[GESTE == "FENMOD", DV := 20]
cout_geste_branche[GESTE == "FEN_MURMOD", DV := 20]
cout_geste_branche[GESTE == "FEN_MURBBC", DV := 20]
cout_geste_branche[GESTE == "GTB", DV := 5]
cout_geste_branche[GESTE == "Etat initial", DV := 1]

#### part de marché des gestes :

cout_geste_branche[GESTE == "ENSBBC", PM := 0.0005]
cout_geste_branche[GESTE == "ENSMOD", PM := 0.001]
cout_geste_branche[GESTE == "FEN_MURBBC", PM := 0.0025]
cout_geste_branche[GESTE == "FEN_MURMOD", PM := 0.005]
cout_geste_branche[GESTE == "FENBBC", PM := 0.0025]
cout_geste_branche[GESTE == "FENMOD", PM := 0.005]

cout_geste_branche[GESTE == "GTB", PM := 0.015]

cout_geste_branche[GESTE == "Etat initial", PM := 0]
cout_geste_branche[,sum(PM, na.rm = T),by="BRANCHE"] 

cout_geste_branche[, PM_ref := 1 - sum(PM, na.rm = T),by="BRANCHE" ]
cout_geste_branche[GESTE == "Etat initial",PM:=PM_ref]


write.table( cout_geste_branche[,list(BRANCHE, GESTE, CHARGE_INIT, COUT_MOY, GAIN_MOY, PART_MARCHE = PM, DUREE_VIE = DV)], "../docs_completementaires_EN/DATAforCINTcalib.csv", sep=";", dec=".", quote = T, row.names = F)


#### graph cout gains

ggplot(data_couts_gestes[GESTE != "Etat initial" & COUT_ENS_MOY_M2 !=0]) +
  geom_point(aes(GAIN_TOT, COUT_ENS_MOY_M2, color=GESTE))

ggplot(data_couts_gestes[GESTE != "Etat initial" & COUT_FEN_MOY_M2 !=0]) +
  geom_point(aes(GAIN_FEN, COUT_FEN_MOY_M2, color=GESTE))

ggplot(data_couts_gestes[GESTE != "Etat initial" & COUT_FENMUR_MOY_M2 !=0]) +
  geom_point(aes(GAIN_FENMUR, COUT_FENMUR_MOY_M2, color=GESTE))

ggplot(data_couts_gestes[GESTE != "Etat initial" & COUT_ENS_MOY_M2 !=0]) +
  geom_point(aes(GAIN_ENS, COUT_ENS_MOY_M2, color=GESTE))

