require(lpSolve)
require(data.table)
require(ggplot2)


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

##### besoins init chauffage
besoins_chauffage_init = fread("../tertiaire/parametrage modèle/Besoin_chauffage_init.csv", dec = ",", 
                               colClasses =
                                 list("character"=c("ID_segment","ID_AGREG2", "ID_AGREG", "ID_AGREG_RDT_COUT", 
                                                    "ID","ID_BRANCHE","ID_BAT_TYPE","ID_PRODUCTION_CHAUD","ID_ENERGIE"),
                               "numeric"= c("RDT systeme")))

besoins_chauffage_init[,ID_AGREG := paste0(substring(ID, 1,6), substring(ID, 9,12))] 
besoins_chauffage_init[,ID_AGREG_calibCINT := paste0(substring(ID, 1,8), substring(ID, 9,10))] 
summary(besoins_chauffage_init )

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
besoins_chauffage_init[,CHARGE_INIT_MOY_M2 := CHARGE_INIT/`SURFACES 2009 recal`]

besoins_chauffage_init[,COD_BRANCHE := substring(ID_AGREG, 1,2)]
besoins_chauffage_init[,COD_SS_BRANCHE := substring(ID_AGREG, 3,4)]
besoins_chauffage_init[,COD_BAT_TYPE := substring(ID_AGREG, 5,6)]
besoins_chauffage_init[,COD_PERIODE_DETAIL := substring(ID_AGREG, 7,8)] 
besoins_chauffage_init[,COD_PERIODE_SIMPLE := substring(ID_AGREG, 9,10)]


#### bibli gestes 
bibli_couts = fread("../docs_completementaires_EN/Bibli_geste_bati_new.csv", sep=";", dec=".", colClasses =
        list("character"="ID_AGREG"))


#####  listes des bâtiments avec aucun geste 
liste_ID_parc = besoins_chauffage_init[,list(ID_AGREG  ,
                                             `SURFACES 2009`,BESOIN_U, ID_BRANCHE, ID_BAT_TYPE )] 

liste_ID_parc[ID_AGREG %in% bibli_couts$ID_AGREG == F]
liste_ID_parc[ID_AGREG %in% bibli_couts[COUT !=0 & EXIGENCE != "Maximale"]$ID_AGREG == F]
liste_ID_parc[ID_AGREG %in% bibli_couts[COUT !=0 & EXIGENCE != "Maximale"]$ID_AGREG == F, unique(ID_BAT_TYPE)]


##### Ajout geste ne rien faire 

CINT_bati_Etat_ini <- besoins_chauffage_init

CINT_bati_Etat_ini$GESTE = "Etat initial"
CINT_bati_Etat_ini[,COUT:= 0]
CINT_bati_Etat_ini[,GAIN := 0]

CINT_bati= CINT_bati_Etat_ini
gestetmp = "FENMOD"


for(gestetmp in unique(bibli_couts[EXIGENCE != "Maximale",GESTE])){
  if(gestetmp != "Etat initial"){
    CINT_bati_tmp = besoins_chauffage_init
    CINT_bati_tmp  = merge(CINT_bati_tmp, bibli_couts[EXIGENCE != "Maximale" & GESTE == gestetmp,
                                                            list(ID_AGREG , 
                                                                 GESTE,COUT, GAIN)], by="ID_AGREG", all.x=T)
  
    CINT_bati = rbind(CINT_bati,CINT_bati_tmp)
    
  }
}




CINT_bati = merge(CINT_bati, COD_BRANCHE, by="COD_BRANCHE", all.x=T)
CINT_bati = merge(CINT_bati, COD_SS_BRANCHE, by="COD_SS_BRANCHE", all.x=T)
CINT_bati = merge(CINT_bati, COD_BAT_TYPE, by="COD_BAT_TYPE", all.x=T)
CINT_bati = merge(CINT_bati, COD_PERIODE_DETAIL, by="COD_PERIODE_DETAIL", all.x=T)
CINT_bati = merge(CINT_bati, COD_PERIODE_SIMPLE, by="COD_PERIODE_SIMPLE", all.x=T)

#### DV des gestes 

CINT_bati[GESTE == "ENSMOD", DV := 50]
CINT_bati[GESTE == "ENSBBC", DV := 50]
CINT_bati[GESTE == "FENBBC", DV := 20]
CINT_bati[GESTE == "FENMOD", DV := 20]
CINT_bati[GESTE == "FEN_MURMOD", DV := 20]
CINT_bati[GESTE == "FEN_MURBBC", DV := 20]
CINT_bati[GESTE == "GTB", DV := 5]
CINT_bati[GESTE == "Etat initial", DV := 1]


# CINT_bati[GESTE == "ENSMOD", DV := 30]
# CINT_bati[GESTE == "ENSBBC", DV := 30]
# CINT_bati[GESTE == "FENBBC", DV := 10]
# CINT_bati[GESTE == "FENMOD", DV := 10]
# CINT_bati[GESTE == "FEN_MURMOD", DV := 20]
# CINT_bati[GESTE == "FEN_MURBBC", DV := 20]
# CINT_bati[GESTE == "GTB", DV := 5]
# CINT_bati[GESTE == "Etat initial", DV := 1]

#####  listes des bâtiments avec seulement GTB

CINT_bati[COUT == 0 & GESTE != "Etat initial", .N, by="BAT_TYPE"]
CINT_bati[BAT_TYPE == "Piscine" & (COUT !=0 | GESTE == "Etat initial"), .N, by="GESTE"]
CINT_bati[,unique(BAT_TYPE)][CINT_bati[,unique(BAT_TYPE)]  %in% CINT_bati[COUT != 0 & GESTE != "Etat initial" & GESTE != "GTB",unique(BAT_TYPE)] == F]



#### taux de rénovation global 
Taux_renov = 0.025

#### part de marché des gestes :

CINT_bati[GESTE == "ENSBBC", PM := 0.001]
CINT_bati[GESTE == "ENSMOD", PM := 0.002]
CINT_bati[GESTE == "FEN_MURBBC", PM := 0.002]
CINT_bati[GESTE == "FEN_MURMOD", PM := 0.004]
CINT_bati[GESTE == "FENBBC", PM := 0.002]
CINT_bati[GESTE == "FENMOD", PM := 0.004]

CINT_bati[GESTE == "GTB", PM := 0.01]
CINT_bati[GESTE == "Etat initial", PM := 1- Taux_renov]

0.001+0.002+0.002+0.004+0.002+0.004+0.01-Taux_renov

#### enlève les gestes à coûts nuls et les ajoute à NRF

CINT_bati[ COUT == 0 & GESTE != "Etat initial",]
length(unique(CINT_bati$ID))

CINT_bati[ID == "010142010701010101",]
CINT_bati[ID == "010142010701010101", 1-sum(PM)]

CINT_bati[ COUT == 0 & GESTE != "Etat initial",PM:=0, by="ID"]
CINT_bati[ GESTE != "Etat initial" & is.na(PM),PM:=0,  by="ID"]

CINT_bati[ID == "010142010701010101",]
CINT_bati[ID == "010142010701010101", 1-sum(PM)]


PM_NRF_add = CINT_bati[GESTE != "Etat initial",list(PM_NRF = 1-sum(PM)), by="ID"]
PM_NRF_add[ID == "010142010701010101",]

summary(PM_NRF_add )
CINT_bati = merge(CINT_bati, PM_NRF_add, by="ID", all.x = T)

CINT_bati[ GESTE == "Etat initial" ,PM:= PM_NRF, by="ID"]
CINT_bati[ ,sum(PM), by="ID"][V1!=1]

CINT_bati[ID == "010142010701010101", 1-sum(PM)]

##### Calcul CG actualisé sans CINT

CINT_bati[substring(ID,5,6) == "05", taux_actu := 0.07]
CINT_bati[substring(ID,5,6) != "05", taux_actu := 0.04]

CINT_bati[, CA :=  (1-(1/(1+taux_actu))^DV)/(taux_actu)]
CINT_bati[, COUT_VARIABLE := COUT/CA + CHARGE_INIT_MOY_M2*(1-GAIN)]

#### PM théorique par segment
CINT_bati[ ,GESTE := factor(GESTE,levels = c("Etat initial","ENSBBC","ENSMOD","FEN_MURBBC","FEN_MURMOD","FENBBC","FENMOD","GTB"))]

nu = 8

CINT_bati[, CG_nu :=  COUT_VARIABLE^(-nu)]
CINT_bati[, PM_theo :=  CG_nu/sum(COUT_VARIABLE^(-nu)), by=c("ID")]

CINT_bati[,sum(PM_theo), by="ID"]

ggplot(CINT_bati[GESTE != "Etat initial"]) + geom_point(aes(PM_theo , PM, color = BRANCHE)) + facet_wrap(~GESTE)
 
##### couts et gains moyens 
CINT_bati[COUT>0 | GESTE ==  "Etat initial",list(COUT_VARIABLE = mean(COUT_VARIABLE), 
                CHARGES = mean(CHARGE_INIT_MOY_M2*(1-GAIN)), 
                CINV = mean(COUT),
                CINVdivCA = mean(COUT/CA), 
                GAIN = mean(GAIN)), by="GESTE"][order(GESTE)]

CINT_bati[COUT>0 | GESTE  ==  "Etat initial" ,
          list(COUT_VARIABLE = mean(COUT_VARIABLE), 
                CHARGES = mean(CHARGE_INIT_MOY_M2*(1-GAIN)), 
                CINV = mean(COUT),
                CINVdivCA = mean(COUT/CA), 
                GAIN = mean(GAIN)), by=c("GESTE","BRANCHE")][order(BRANCHE,GESTE)]


######
require(stats)
require(nleqslv)

listID = unique(CINT_bati$ID)

nu <- 8

CG = function(CINT, geste, id_segment){
  CG <- CINT_bati[ID == id_segment & GESTE==geste, COUT_VARIABLE] + CINT
  CG  
  
}


CG(x = 10,  "Etat initial", listID[1])


CINT_bati[, CG(10,GESTE,ID)^-nu]

PM = function(x,geste,id_segment){
  
  tabdata =  CINT_bati[ID == id_segment, list(ID,GESTE)]
  tabdata[GESTE == "Etat initial", CINT := x[1]]
  tabdata[GESTE == "ENSBBC", CINT := x[2]]
  tabdata[GESTE == "ENSMOD", CINT := x[3]]
  tabdata[GESTE == "FEN_MURBBC", CINT := x[4]]
  tabdata[GESTE == "FEN_MURMOD", CINT := x[5]]
  tabdata[GESTE == "FENBBC", CINT := x[5]]
  tabdata[GESTE == "FENMOD", CINT := x[6]]
  tabdata[GESTE == "GTB", CINT := x[7]]
  sumCG = tabdata[ID == id_segment, sum(CG(CINT,GESTE,ID)^-nu)]
  PM <-  tabdata[ID == id_segment & GESTE == geste, CG(CINT,GESTE,ID)^-nu/sumCG]
  PM
}

PM(rep(0,8),"Etat initial", listID[1])
CINT_bati[ID == listID[1],PM_theo]

PM(c(0,10,10,5,5,2,2,2),"Etat initial", listID[1])
PM(c(0,10,10,5,5,2,2,2),"ENSBBC", listID[1])

#### taux de rénovation global 
Taux_renov = 0.025

#### part de marché des gestes :
PM_obj = data.table(GESTE = unique(CINT_bati$GESTE))
PM_obj 

PM_obj[GESTE == "Etat initial", PM := 1- Taux_renov]
PM_obj[GESTE == "ENSBBC", PM := 0.001]
PM_obj[GESTE == "ENSMOD", PM := 0.002]
PM_obj[GESTE == "FEN_MURBBC", PM := 0.002]
PM_obj[GESTE == "FEN_MURMOD", PM := 0.004]
PM_obj[GESTE == "FENBBC", PM := 0.002]
PM_obj[GESTE == "FENMOD", PM := 0.004]
PM_obj[GESTE == "GTB", PM := 0.01]

Stot*PM_obj[GESTE =="Etat initial",PM] 

lambda <- 0.1



CINT_bati[substring(ID,0,2) %in% "01" ,]
unique(CINT_bati$GESTE)


systCINT <- function(x){
  y <-c()
  
  
  Stot = sum(Datasyst[GESTE == "Etat initial",`SURFACES 2009 recal`])
  
  neq = 0
  ngeste = 0

  for(geste in  unique(Datasyst$GESTE)){
    ngeste <- ngeste + 1
    Datasyst[GESTE == geste,CINT := x[ngeste]^2, by=c("ID")]
#    Datasyst[GESTE == geste,CG := CG(CINT,GESTE,ID), by=c("ID")]
    Datasyst[GESTE == geste,CG :=  COUT_VARIABLE +  x[ngeste]^2, by=c("ID")]
  }
  
  Datasyst[,PMtmp := CG^(-nu)/sum(CG^(-nu)), by=c("ID")]

  neq = 0
  for(geste in unique(Datasyst$GESTE)[1:(length(unique(Datasyst$GESTE))-1)]){
    neq <- neq + 1
    y[neq] <-   PM_obj[GESTE ==geste,PM] - Datasyst[GESTE == geste,sum(PMtmp*`SURFACES 2009 recal`)]/Stot
  }
 
  y[neq + 1] <- lambda -  Datasyst[,sum(CINT*`SURFACES 2009 recal`)]/Datasyst[,sum(CG*`SURFACES 2009 recal`)]
  y
  }


systCINTnopos <- function(x){
  y <-c()
  
  
  Stot = sum(Datasyst[GESTE == "Etat initial",`SURFACES 2009 recal`])
  
  neq = 0
  ngeste = 0
  
  for(geste in  unique(Datasyst$GESTE)){
    ngeste <- ngeste + 1
    Datasyst[GESTE == geste,CINT := x[ngeste], by=c("ID")]
    #    Datasyst[GESTE == geste,CG := CG(CINT,GESTE,ID), by=c("ID")]
    Datasyst[GESTE == geste,CG :=  COUT_VARIABLE +  x[ngeste], by=c("ID")]
  }
  
  Datasyst[,PMtmp := CG^(-nu)/sum(CG^(-nu)), by=c("ID")]
  
  neq = 0
  for(geste in unique(Datasyst$GESTE)[1:(length(unique(Datasyst$GESTE))-1)]){
    neq <- neq + 1
    y[neq] <-   PM_obj[GESTE ==geste,PM] - Datasyst[GESTE == geste,sum(PMtmp*`SURFACES 2009 recal`)]/Stot
  }
  
  y[neq + 1] <- lambda -  Datasyst[,sum(CINT*`SURFACES 2009 recal`)]/Datasyst[,sum(CG*`SURFACES 2009 recal`)]
  y
}
Datasyst <- CINT_bati[ (COUT !=0 | GESTE == "Etat initial"),]
Datasyst <- Datasyst[order(GESTE)]
x<-rep(0,8)

lambda <- 0.05

xnoCINT  = rep(0,8)
systCINT(xnoCINT)
#x = rep(0,8)

x0 = c(0,10,10,8,8,6,6,2) 
systCINT(x0)



#### resultats ensemble et par branche sans CINT
Datasyst <- CINT_bati[ (COUT !=0 | GESTE == "Etat initial"),]
Datasyst <- Datasyst[order(GESTE)]
x0 = rep(0, length( unique(Datasyst$GESTE)))
round(systCINTnopos(x0), digits = 5)

for(Branche in c(paste0("0",1:8))){
Datasyst <- CINT_bati[substring(ID,0,2) %in% Branche & (COUT !=0 | GESTE == "Etat initial"),]
Datasyst <- Datasyst[order(GESTE)]
x0 = rep(0, length( unique(Datasyst$GESTE)))
print(Branche)
print(round(systCINTnopos(x0), digits = 5))
}

#### resultats ensemble et par branche sans CINT avec cout ensemble BBC fois 2
Datasyst <- CINT_bati[ (COUT !=0 | GESTE == "Etat initial"),]
Datasyst <- Datasyst[order(GESTE)]
Datasyst[GESTE == "ENSBBC", COUT := COUT*2]
Datasyst[GESTE == "ENSMOD", COUT := COUT*2]
#Datasyst[, COUT := COUT*3]

Datasyst[, COUT_VARIABLE := COUT/CA + CHARGE_INIT_MOY_M2*(1-GAIN)]

x0 = rep(0, length( unique(Datasyst$GESTE)))
round(systCINTnopos(x0), digits = 5)

for(Branche in c(paste0("0",1:8))){
  Datasyst <- CINT_bati[substring(ID,0,2) %in% Branche & (COUT !=0 | GESTE == "Etat initial"),]
  Datasyst <- Datasyst[order(GESTE)]
  Datasyst[GESTE == "ENSBBC", COUT := COUT*2]
  Datasyst[GESTE == "ENSMOD", COUT := COUT*2]
  #Datasyst[, COUT := COUT*4]
  Datasyst[, COUT_VARIABLE := COUT/CA + CHARGE_INIT_MOY_M2*(1-GAIN)]
  
  x0 = rep(0, length( unique(Datasyst$GESTE)))
  print(Branche)
  print(round(systCINTnopos(x0), digits = 5))
}


#results = nleqslv(x0,systCINT,  control= list(ftol =0.00001))

#Datasyst <- CINT_bati[ID_BAT_TYPE %in% "84" & (COUT !=0 | GESTE == "Etat initial"),]
#Datasyst <- CINT_bati[substring(ID,0,2) %in% "01" & (COUT !=0 | GESTE == "Etat initial"),]
Datasyst <- CINT_bati[ (COUT !=0 | GESTE == "Etat initial"),]
Datasyst[ ,GESTE := factor(GESTE,levels = c("Etat initial","ENSBBC","ENSMOD","FEN_MURBBC","FEN_MURMOD","FENBBC","FENMOD","GTB"))]
Datasyst <- Datasyst[order(GESTE)]
Datasyst[GESTE == "ENSBBC", COUT := COUT*2]
Datasyst[GESTE == "ENSMOD", COUT := COUT*2]
#Datasyst[, COUT := COUT*4]
Datasyst[, COUT_VARIABLE := COUT/CA + CHARGE_INIT_MOY_M2*(1-GAIN)]
Datafinal <- Datasyst

x0 = rep(0, length( unique(Datasyst$GESTE)))
systCINT(x0)
round(systCINT(x0), digits = 3)
round(systCINTnopos(x0), digits = 5)

lambdamin <- 0.05
for(lambda in seq(lambdamin ,2,0.01)){
print(lambda)
results = nleqslv(x0,systCINT,  control= list(ftol =0.0001))
#results = nleqslv(x0,systCINTnopos,  control= list(ftol =0.000001))
print(results$termcd)
if(results$termcd ==1){
  lastlambda = lambda
  resultsfinal = results
  break
}

}

xsol =resultsfinal$x
systCINTnopos(xsol )
round(systCINTnopos(xsol), digits = 5)


ngeste = 0

Stot = sum(Datafinal [GESTE == "Etat initial",`SURFACES 2009 recal`])
for(geste in unique(Datafinal$GESTE)){
  ngeste <- ngeste + 1
  Datafinal[GESTE == geste,CINTsol :=xsol[ngeste], by=c("ID")]
  Datafinal[GESTE == geste,CGsol := COUT_VARIABLE+CINTsol, by=c("ID")]
}

##### vérificationj des PM obtenues 

Datafinal[,PMsol := CGsol^(-nu)/sum(CGsol^(-nu)), by=c("ID")]
Datafinal[,sum(PMsol), by="ID"][V1 <0.99,V1]

Datafinal[,Srenov := PMsol*`SURFACES 2009 recal`, by=c("ID","GESTE")]

Datafinal[,sum(Srenov), by=c("GESTE")]


Datafinal[,sum(Srenov), by=c("GESTE")][,list(GESTE,PM = round(V1/Stot, digits = 6))]
sum(Datafinal[,sum(Srenov), by=c("GESTE")][,V1/Stot])

#### vérif part des CINT
Datafinal[,sum(CINTsol*`SURFACES 2009 recal`)]/Datafinal[,sum(CGsol*`SURFACES 2009 recal`)]


#### CG négatif
Datafinal[CGsol <0, unique(GESTE)]
Datafinal[CGsol <0, CGsol]


##### système que sur le ne rien ##### 

PM_obj_NRF = 0.975
systCINT_NRF <- function(x){
  y <-c()
  
  
  Stot = sum(Datasyst[GESTE == "Etat initial",`SURFACES 2009 recal`])
  
  neq = 0
  ngeste = 0
  
  for(geste in  unique(Datasyst$GESTE)){
    ngeste <- ngeste + 1
    Datasyst[GESTE == geste,CINT := 1, by=c("ID")]
    if(geste == "Etat initial"){
      Datasyst[GESTE == geste,CINT := x[ngeste]^2, by=c("ID")]
    }
    #    Datasyst[GESTE == geste,CG := CG(CINT,GESTE,ID), by=c("ID")]
    Datasyst[GESTE == geste,CG :=  COUT_VARIABLE*CINT, by=c("ID")]
  }
  
  
  Datasyst[,PMtmp := CG^(-nu)/sum(CG^(-nu)), by=c("ID")]
  
  neq = 1
  y[neq] <-   PM_obj_NRF - Datasyst[GESTE ==  "Etat initial",sum(PMtmp*`SURFACES 2009 recal`)]/Stot
  y
}

Datasyst <- CINT_bati[ (COUT !=0 | GESTE == "Etat initial"),]
Datasyst[ ,GESTE := factor(GESTE,levels = c("Etat initial","ENSBBC","ENSMOD","FEN_MURBBC","FEN_MURMOD","FENBBC","FENMOD","GTB"))]
Datasyst <- Datasyst[order(GESTE)]
# Datasyst[GESTE == "ENSBBC", COUT := COUT*2]
# Datasyst[GESTE == "ENSMOD", COUT := COUT*2]
#Datasyst[, COUT := COUT*4]
Datasyst[, COUT_VARIABLE := COUT/CA + CHARGE_INIT_MOY_M2*(1-GAIN)]
Datafinal <- Datasyst

x0 = 1
systCINT_NRF(x0)
results = nleqslv(x0,systCINT_NRF,  control= list(ftol =0.00000001))
 
print(results$termcd)
if(results$termcd ==1){
    resultsfinal = results
}
  
xsol =resultsfinal$x
systCINT_NRF(xsol )
round(systCINT_NRF(xsol), digits = 5)


ngeste = 0

Stot = sum(Datafinal [GESTE == "Etat initial",`SURFACES 2009 recal`])

Datafinal[GESTE == "Etat initial",CINTsol :=xsol[1]^2, by=c("ID")]
Datafinal[GESTE == "Etat initial",CGsol := COUT_VARIABLE*CINTsol, by=c("ID")]
Datafinal[GESTE != "Etat initial",CGsol := COUT_VARIABLE, by=c("ID")]


##### vérificationj des PM obtenues 

Datafinal[,PMsol := CGsol^(-nu)/sum(CGsol^(-nu)), by=c("ID")]
Datafinal[,sum(PMsol), by="ID"][V1 <0.99,V1]

Datafinal[,Srenov := PMsol*`SURFACES 2009 recal`, by=c("ID","GESTE")]

Datafinal[,sum(Srenov), by=c("GESTE")]


Datafinal[,sum(Srenov), by=c("GESTE")][,list(GESTE,PM = round(V1/Stot, digits = 6))]
sum(Datafinal[,sum(Srenov), by=c("GESTE")][,V1/Stot])

###### 
summary(Datafinal[COUT>0 ])

ggplot(Datafinal[COUT>0 | GESTE == "Etat initial"])+geom_point( aes(PM_theo, PMsol, color= GESTE))

Datafinal[PMsol>0.5 & GESTE != "Etat initial"]

###### coûts d'investissements totaux
Datafinal[,sum(Srenov*COUT)/10^9, by=c("GESTE")]
Datafinal[,sum(Srenov*COUT)/10^9]

##### ##### ##### ##### ##### ##### ##### ##### ##### ##### ##### ##### ##### 
##### pertinence des systèmes de chauffage
##### ##### ##### ##### ##### ##### ##### ##### ##### ##### ##### ##### ##### ##### ##### ##### 

##### données sur les syst
bibli_syst = fread("table_param_origine/Bibli_rdt_chauff.csv", sep=";", dec=".", colClasses = c("ID_AGREG"="character"))

DV_syst = fread("DV_syst.csv",sep=";", encoding = "UTF-8", colClasses = c("maintenance"="numeric"))

bibli_syst <- bibli_syst[,list(ID_AGREG,PERIODE,RDT,COUT,CEE)]
bibli_syst[, COD_BRANCHE:=substring(ID_AGREG, 1,2)]
bibli_syst[, COD_SS_BRANCHE:=substring(ID_AGREG, 3,4)]
bibli_syst[, COD_BAT_TYPE:=substring(ID_AGREG, 5,6)]
bibli_syst[, COD_SYSTEME_CHAUD:=substring(ID_AGREG, 7,8)]
bibli_syst[, COD_ENERGIE:=substring(ID_AGREG, 9,10)]

bibli_syst= merge(bibli_syst, COD_BRANCHE, by="COD_BRANCHE")
bibli_syst= merge(bibli_syst, COD_SS_BRANCHE, by="COD_SS_BRANCHE")
bibli_syst= merge(bibli_syst, COD_BAT_TYPE, by="COD_BAT_TYPE")
bibli_syst= merge(bibli_syst, COD_ENERGIE, by="COD_ENERGIE")
bibli_syst= merge(bibli_syst, COD_SYSTEME_CHAUD, by="COD_SYSTEME_CHAUD")

bibli_syst = merge(bibli_syst,DV_syst, by="SYSTEME_CHAUD")
#### coef actualisation
taux_actu = 0.05
bibli_syst[, CA :=  (1-(1/(1+taux_actu))^DV)/(taux_actu)]
bibli_syst[, CA2 :=  (1-(1/(1+taux_actu))^DV2)/(taux_actu)]

setnames(bibli_syst, "SYSTEME_CHAUD","PRODUCTION_CHAUD")
bibli_syst[,PRODUCTION_CHAUD :=factor(PRODUCTION_CHAUD, 
                                                  levels = c("Chaudière gaz","Chaudière condensation gaz",
                                                             "Tube radiant", "Tube radiant performant" ,
                                                             "Chaudière fioul","Chaudière condensation fioul",
                                                             "Electrique direct","Electrique direct performant",
                                                             "Cassette rayonnante","Cassette rayonnante performant",
                                                             "PAC","PAC performant",
                                                             "Rooftop", "Rooftop performant",
                                                             "DRV", "DRV performant",
                                                             "Autre système centralisé",  "Autre système centralisé performant",
                                                             "nr","NA"))]

colors_syst= 
  data.table(PRODUCTION_CHAUD  =factor(unique(COD_SYSTEME_CHAUD$SYSTEME_CHAUD), 
                                   levels = c("Chaudière gaz","Chaudière condensation gaz",
                                              "Tube radiant", "Tube radiant performant" ,
                                              "Chaudière fioul","Chaudière condensation fioul",
                                              "Electrique direct","Electrique direct performant",
                                              "Cassette rayonnante","Cassette rayonnante performant",
                                              "PAC","PAC performant",
                                              "Rooftop", "Rooftop performant",
                                              "DRV", "DRV performant",
                                              "Autre système centralisé",  "Autre système centralisé performant",
                                              "nr","NA"))
  )

colors_syst = colors_syst[order(PRODUCTION_CHAUD )]
colors_syst[,color := c("dodgerblue1","dodgerblue4", "blue1","blue4","springgreen1","springgreen4",
                        "yellow1","yellow4","gold1","gold4","chocolate1","chocolate4","coral1","coral3",
                        "salmon1","salmon4","red1","red4","gray86","gray86")]
colors_syst = colors_syst[order(PRODUCTION_CHAUD )]
bibli_syst = merge(bibli_syst,colors_syst, by="PRODUCTION_CHAUD")
bibli_syst[,COUT_MAIN := COUT*maintenance]
##### moyenne par système et bâtiment pour les bâtiments neufs 
bibli_syst_neuf  <- bibli_syst
bibli_syst_neuf = bibli_syst_neuf[PERIODE==1,list(COUT=mean(COUT),COUT_MAIN = mean(COUT_MAIN),RDT = mean(RDT),DV=mean(DV), DV2=mean(DV2), 
                                                  CA=mean(CA), CA2=mean(CA2)),
                                  by=c("BRANCHE","BAT_TYPE","PRODUCTION_CHAUD",
                                       "ENERGIE","COD_ENERGIE","color")]

bibli_syst_neuf[COD_ENERGIE == "01", prix_2010 := 0.050947368]
bibli_syst_neuf[COD_ENERGIE == "02", prix_2010 := 0.09847	]
bibli_syst_neuf[COD_ENERGIE == "03", prix_2010 := 0.076392929]
bibli_syst_neuf[COD_ENERGIE == "04", prix_2010 := 0.038671967]
bibli_syst_neuf[COD_ENERGIE == "06", prix_2010 := 0.0591]
bibli_syst_neuf[COD_ENERGIE == "05", prix_2010 := 0]

### conso bâtiment neuf

conso_entrants = fread("conso_unitaire_batiments_entrants_AME.csv")
conso_entrants = melt(conso_entrants,id.vars = c("BRANCHE","BAT_TYPE","USAGE"), 
                      variable.name = "période",
                      value.name = "conso_unitaire")

conso_entrants[ ,USAGE := factor(USAGE, levels = c("Chauffage","Climatisation","Auxiliaires","Ventilation","ECS",
                                                   "Eclairage",
                                                   "Bureautique","Process","Froid_alimentaire","Autre","Cuisson"))]

setnames(conso_entrants,"période","PERIODE_simu")
setnames(conso_entrants,"conso_unitaire","BESOIN_U")

#### merge avec les données sur les besoins

data_syst_exi <-  merge(besoins_chauffage_init, 
                      bibli_syst[PERIODE==1,list(ID_AGREG_RDT_COUT = ID_AGREG,PRODUCTION_CHAUD,ENERGIE,COUT,COUT_MAIN,
                                                 RDT,DV,DV2,CA,CA2,color)],
                      by="ID_AGREG_RDT_COUT")

data_syst_exi [,CONSO_U := BESOIN_U/RDT]
data_syst_exi  <- data_syst_exi [ID_ENERGIE!="01"]
data_syst_exi <- merge(data_syst_exi,COD_BAT_TYPE,by="COD_BAT_TYPE")
data_syst_exi <- merge(data_syst_exi,COD_PERIODE_SIMPLE,by="COD_PERIODE_SIMPLE")
data_syst_exi <- merge(data_syst_exi,COD_PERIODE_DETAIL,by="COD_PERIODE_DETAIL")


# data_syst_neuf_avantRT = merge(bibli_syst_neuf,conso_entrants[USAGE=="Chauffage" & PERIODE_simu == "2010-2015"],
#                                by=c("BRANCHE","BAT_TYPE"))
# data_syst_neuf_apresRT = merge(bibli_syst_neuf,conso_entrants[USAGE=="Chauffage" & PERIODE_simu == "2015-2020"], 
#                                by=c("BRANCHE","BAT_TYPE"))
data_syst_neuf <- merge(bibli_syst_neuf,conso_entrants[USAGE=="Chauffage" & PERIODE_simu == "2010-2015",
                                                       list(BRANCHE,BAT_TYPE,BESOIN_U_avantRT = BESOIN_U)],
                        by=c("BRANCHE","BAT_TYPE"))
data_syst_neuf <- merge(data_syst_neuf,conso_entrants[USAGE=="Chauffage" & PERIODE_simu == "2015-2020",
                                                                      list(BRANCHE,BAT_TYPE,BESOIN_U_apresRT = BESOIN_U)]
                        ,
                        by=c("BRANCHE","BAT_TYPE"))

################################################
#### pertinence dans le neuf
#################################################

data_syst_neuf [,CONSO_U_avantRT := BESOIN_U_avantRT/RDT]
data_syst_neuf [,CONSO_U_apresRT := BESOIN_U_apresRT/RDT]

data_syst_neuf  <- data_syst_neuf [COD_ENERGIE!="01"]
BAT_TMP = "immeuble rénové"

data_syst_neuf_eleccoutbas <- data_syst_neuf 
data_syst_neuf_eleccoutbas[ENERGIE == "Electricté", COUT := 0.5*COUT]

for(BAT_TMP in unique( data_syst_neuf$BAT_TYPE)){
  
  data_plot = data_syst_neuf [BAT_TYPE==BAT_TMP]
  data_plot[,CONSO_U_PLOT := 0]
  
  data_plot_eleccoutbas = data_syst_neuf [BAT_TYPE==BAT_TMP]
  data_plot_eleccoutbas[,CONSO_U_PLOT := 0]
  
  for(C in seq(0,200,1)){
    data_tmp = data_syst_neuf [BAT_TYPE==BAT_TMP]
    data_tmp [,CONSO_U_PLOT := C]
  data_plot =rbind(data_plot,data_tmp)
  }
  
  ggplot(data_plot) + 
    geom_line( aes(CONSO_U_PLOT*RDT, COUT/CA + COUT_MAIN + CONSO_U_PLOT*prix_2010, group=PRODUCTION_CHAUD,color =PRODUCTION_CHAUD ),size = 2) +   
    scale_color_manual(values=unique(data_plot$color), guide = guide_legend(nrow = 4)) + 
    ylab("euros par m²") + xlab("kWh par m²") + 
    ggtitle(paste(unique(data_plot$BAT_TYPE),"neuf")) + theme_bw() + 
    scale_x_continuous(limits = c(0,max(data_plot$CONSO_U_avantRT*data_plot$RDT)*1.5)) + 
    scale_y_continuous(limits = c(0,max(data_plot[,COUT/CA+ COUT_MAIN +CONSO_U_avantRT*prix_2010])*1.5)) +
    theme(legend.position = "top")+
    geom_vline(aes(xintercept = CONSO_U_avantRT*RDT ), size=2)  +
    geom_vline(aes(xintercept = CONSO_U_apresRT*RDT ), size=2)
  
  ggsave(filename = paste("Pertinence_syst/Dom Pert syst",BAT_TMP,"neuf",".png"))
  
  ggplot(data_plot) + 
    geom_line( aes(CONSO_U_PLOT*RDT, COUT/CA2 + COUT_MAIN + CONSO_U_PLOT*prix_2010, group=PRODUCTION_CHAUD,color =PRODUCTION_CHAUD ),size = 2) +   
    scale_color_manual(values=unique(data_plot$color), guide = guide_legend(nrow = 4)) + 
    ylab("euros par m²") + xlab("kWh par m²") + 
    ggtitle(paste(unique(data_plot$BAT_TYPE),"neuf")) + theme_bw() + 
    scale_x_continuous(limits = c(0,max(data_plot$CONSO_U_avantRT*data_plot$RDT)*1.5)) + 
    scale_y_continuous(limits = c(0,max(data_plot[,COUT/CA2+CONSO_U_avantRT*prix_2010])*1.5)) +
    theme(legend.position = "top")+
    geom_vline(aes(xintercept = CONSO_U_avantRT*RDT ), size=2)  +
    geom_vline(aes(xintercept = CONSO_U_apresRT*RDT ), size=2)
  
  ggsave(filename = paste("Pertinence_syst/Dom Pert syst",BAT_TMP,"neuf DV20",".png"))
}


data_plot_mean <- data_syst_neuf[COD_ENERGIE!="06",list(COUT=mean(COUT),COUT_MAIN=mean(COUT_MAIN),
                                                        RDT=mean(RDT),DV=mean(DV), CA=mean(CA),CA2=mean(CA2),
                                                                prix_2010=mean(prix_2010)),by=c("PRODUCTION_CHAUD","COD_ENERGIE","color")]
data_plot_mean [COD_ENERGIE!="06",BESOIN_U_avantRT := mean(data_syst_neuf$BESOIN_U_avantRT)]
data_plot_mean [COD_ENERGIE!="06",BESOIN_U_apresRT := mean(data_syst_neuf$BESOIN_U_apresRT)]
data_plot_mean [,CONSO_U_avantRT := BESOIN_U_avantRT/RDT] 
data_plot_mean [,CONSO_U_apresRT := BESOIN_U_apresRT/RDT] 

data_plot <- data_plot_mean[order(PRODUCTION_CHAUD)]
data_plot[,CONSO_U_PLOT := 0]
for(C in seq(0,200,1)){
  data_tmp = data_plot_mean
  data_tmp [,CONSO_U_PLOT := C]
  data_plot =rbind(data_plot,data_tmp)
}

ggplot(data_plot) + 
  geom_line( aes(CONSO_U_PLOT*RDT, COUT/CA + COUT_MAIN + CONSO_U_PLOT*prix_2010, group=PRODUCTION_CHAUD,color =PRODUCTION_CHAUD ),size = 2) +   
  scale_color_manual(values=unique(data_plot$color), guide = guide_legend(nrow = 4)) + 
  ylab("euros par m²") + xlab("kWh par m²") + 
  ggtitle("Moyenne neuf") + theme_bw() + 
  scale_x_continuous(limits = c(0,max(data_plot$CONSO_U_avantRT*data_plot$RDT)*1.5)) + 
  scale_y_continuous(limits = c(0,max(data_plot[,COUT/CA+ COUT_MAIN +CONSO_U_avantRT*prix_2010])*1.5)) +
  theme(legend.position = "top")+geom_vline(aes(xintercept = CONSO_U_avantRT*RDT ), size=2) +
  geom_vline(aes(xintercept = CONSO_U_apresRT*RDT ), size=2)


ggsave(filename = paste("Pertinence_syst/Dom Pert syst","MOY","neuf",".png"))

ggplot(data_plot) + 
  geom_line( aes(CONSO_U_PLOT*RDT, COUT/CA2 + COUT_MAIN + CONSO_U_PLOT*prix_2010, group=PRODUCTION_CHAUD,color =PRODUCTION_CHAUD ),size = 2) +   
  scale_color_manual(values=unique(data_plot$color), guide = guide_legend(nrow = 4)) + 
  ylab("euros par m²") + xlab("kWh par m²") + 
  ggtitle("Moyenne neuf") + theme_bw() + 
  scale_x_continuous(limits = c(0,max(data_plot$CONSO_U_avantRT*data_plot$RDT)*1.5)) + 
  scale_y_continuous(limits = c(0,max(data_plot[,COUT/CA2+ COUT_MAIN +CONSO_U_avantRT*prix_2010])*1.5)) +
  theme(legend.position = "top")+geom_vline(aes(xintercept = CONSO_U_avantRT*RDT ), size=2) +
  geom_vline(aes(xintercept = CONSO_U_apresRT*RDT ), size=2)


ggsave(filename = paste("Pertinence_syst/Dom Pert syst","MOY","neuf DV20",".png"))

#### pertinence dans l'existant

BAT_TMP = "immeuble rénové"

data_syst_exi <- data_syst_exi[order(PRODUCTION_CHAUD)]
data_syst_exi[BAT_TYPE==BAT_TMP & PRODUCTION_CHAUD=="Chaudière gaz"]

data_syst_exi_mean <- data_syst_exi[ID_ENERGIE!="01", list(COUT = mean(COUT),COUT_MAIN=mean(COUT_MAIN),
                                                           RDT=mean(RDT),DV=mean(DV), CA=mean(CA),CA2=mean(CA2),
                                           BESOIN_U = sum(BESOIN_U*`SURFACES 2009`)/sum(`SURFACES 2009`)),
                                           by=c("COD_BRANCHE","BAT_TYPE","PERIODE_DETAIL",
                                                   "PRODUCTION_CHAUD","ID_ENERGIE","color","prix_2010")]



data_syst_exi_mean[, COUT:=mean(COUT),by=c("BAT_TYPE","PRODUCTION_CHAUD","ID_ENERGIE")]
data_syst_exi_mean[, COUT_MAIN:=mean(COUT_MAIN),by=c("BAT_TYPE","PRODUCTION_CHAUD","ID_ENERGIE")]
data_syst_exi_mean[, RDT:=mean(RDT),by=c("BAT_TYPE","PRODUCTION_CHAUD","ID_ENERGIE")]
data_syst_exi_mean[, BESOIN_U:=mean(BESOIN_U), by=c("BAT_TYPE","PERIODE_DETAIL")]
data_syst_exi_mean[BAT_TYPE==BAT_TMP]

#data_syst_exi_mean <- dcast.data.table(data_syst_exi_mean,...~ PERIODE_DETAIL,value.var = "BESOIN_U")
data_syst_exi_mean[BAT_TYPE==BAT_TMP]
data_syst_exi_mean[,CONSO_U := BESOIN_U/RDT]



data_plot <- data_syst_exi_mean

for(BAT_TMP in unique( data_syst_exi$BAT_TYPE)){
  data_plot <- data_syst_exi_mean[BAT_TYPE==BAT_TMP]
  data_plot[,CONSO_U_PLOT := 0]
  for(C in seq(0,400,10)){
    data_tmp = data_syst_exi_mean[BAT_TYPE==BAT_TMP]
    data_tmp [,CONSO_U_PLOT := C]
    data_plot =rbind(data_plot,data_tmp)
  }
  
  ggplot(data_plot) + 
    geom_line( aes(CONSO_U_PLOT*RDT, COUT/CA +COUT_MAIN + CONSO_U_PLOT*prix_2010, group=PRODUCTION_CHAUD,color =PRODUCTION_CHAUD ),size = 2)  + 
    ylab("euros par m²") + xlab("kWh par m²") + 
    ggtitle(paste(unique(data_plot$BAT_TYPE),"existant")) + theme_bw() + 
    theme(legend.position = "top") + 
    scale_color_manual("",values=unique(data_plot$color), guide = guide_legend(nrow = 4)) +
    scale_x_continuous(limits = c(0,200)) + 
    scale_y_continuous(limits = c(0,max(data_plot[,COUT/CA+ COUT_MAIN +CONSO_U*prix_2010])*1.5)) +
    geom_vline(aes(xintercept =BESOIN_U,linetype=PERIODE_DETAIL ), size=2) +
    scale_linetype_discrete("", guide = guide_legend(nrow = 4)) 
    
  ggsave(filename = paste("Pertinence_syst/Dom Pert syst",BAT_TMP,"existant",".png"))
  
  ggplot(data_plot) + 
    geom_line( aes(CONSO_U_PLOT*RDT, COUT/CA2 +COUT_MAIN + CONSO_U_PLOT*prix_2010, group=PRODUCTION_CHAUD,color =PRODUCTION_CHAUD ),size = 2)  + 
    ylab("euros par m²") + xlab("kWh par m²") + 
    ggtitle(paste(unique(data_plot$BAT_TYPE),"existant")) + theme_bw() + 
    theme(legend.position = "top") + 
    scale_color_manual("",values=unique(data_plot$color), guide = guide_legend(nrow = 4)) +
    scale_x_continuous(limits = c(0,200)) + 
    scale_y_continuous(limits = c(0,max(data_plot[,COUT/CA+ COUT_MAIN +CONSO_U*prix_2010])*1.5)) +
    geom_vline(aes(xintercept =BESOIN_U,linetype=PERIODE_DETAIL ), size=2) +
    scale_linetype_discrete("", guide = guide_legend(nrow = 4)) 
  
  ggsave(filename = paste("Pertinence_syst/Dom Pert syst",BAT_TMP,"existant DV20",".png"))
}


data_syst_exi_mean <- data_syst_exi[ID_ENERGIE!="01" & PRODUCTION_CHAUD !="nr", list(COUT = mean(COUT),COUT_MAIN=mean(COUT_MAIN),
                                                                                     RDT=mean(RDT),DV=mean(DV), CA=mean(CA),CA2=mean(CA2),
                                                                                     BESOIN_U = sum(BESOIN_U*`SURFACES 2009`)/sum(`SURFACES 2009`)),
                                    by=c("PERIODE_DETAIL",
                                         "PRODUCTION_CHAUD","ID_ENERGIE","color","prix_2010")]

data_syst_exi_mean[, COUT:=mean(COUT),by=c("PRODUCTION_CHAUD","ID_ENERGIE")]
data_syst_exi_mean[, COUT_MAIN:=mean(COUT_MAIN),by=c("PRODUCTION_CHAUD","ID_ENERGIE")]
data_syst_exi_mean[, RDT:=mean(RDT),by=c("PRODUCTION_CHAUD","ID_ENERGIE")]
data_syst_exi_mean[, BESOIN_U:=mean(BESOIN_U), by=c("PERIODE_DETAIL")]
data_syst_exi_mean[PERIODE_DETAIL == "Av 1948"]
data_syst_exi_mean[PERIODE_DETAIL == "Av 1974"]

data_syst_exi_mean[,CONSO_U := BESOIN_U/RDT] 
data_plot <- data_syst_exi_mean[order(PRODUCTION_CHAUD)]
data_plot[,CONSO_U_PLOT := 0]
for(C in seq(0,200,10)){
  data_tmp = data_syst_exi_mean
  
  data_tmp [,CONSO_U_PLOT := C]
  data_plot =rbind(data_plot,data_tmp)
}

ggplot(data_plot) + 
  geom_line( aes(CONSO_U_PLOT*RDT, COUT/CA+ CONSO_U_PLOT*prix_2010, group=PRODUCTION_CHAUD,color =PRODUCTION_CHAUD ),size = 2)  + 
  ylab("euros par m²") + xlab("kWh par m²") + 
  ggtitle(paste(unique(data_plot$BAT_TYPE),"existant")) + theme_bw() + 
  theme(legend.position = "top") + 
  scale_color_manual("",values=unique(data_plot$color), guide = guide_legend(nrow = 4)) +
  scale_x_continuous(limits = c(0,200)) + 
  geom_vline(aes(xintercept =BESOIN_U,linetype=PERIODE_DETAIL, fill=PERIODE_DETAIL ), size=2) +
  scale_linetype_discrete("", guide = guide_legend(nrow = 4)) 


ggsave(filename = paste("Pertinence_syst/Dom Pert syst","MOY","existant",".png"))

ggplot(data_plot) + 
  geom_line( aes(CONSO_U_PLOT*RDT, COUT/CA2+ CONSO_U_PLOT*prix_2010, group=PRODUCTION_CHAUD,color =PRODUCTION_CHAUD ),size = 2)  + 
  ylab("euros par m²") + xlab("kWh par m²") + 
  ggtitle(paste(unique(data_plot$BAT_TYPE),"existant")) + theme_bw() + 
  theme(legend.position = "top") + 
  scale_color_manual("",values=unique(data_plot$color), guide = guide_legend(nrow = 4)) +
  scale_x_continuous(limits = c(0,200)) + 
  geom_vline(aes(xintercept =BESOIN_U,linetype=PERIODE_DETAIL, fill=PERIODE_DETAIL ), size=2) +
  scale_linetype_discrete("", guide = guide_legend(nrow = 4)) 


ggsave(filename = paste("Pertinence_syst/Dom Pert syst","MOY","existant DV20",".png"))

