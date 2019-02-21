
#source("analyse_param_utilisateurs.R",encoding = "ISO8859-1")

##### prix des energies ####
prix_energie = fread("../_CGDD_Packaging_27.05.15/Tables_param/sauvegarde para/Prix_scenario_AME_BV.csv")
prix_energie

prix_energie = melt(prix_energie,id.vars = "annee")

prix_energie[variable == "prix_elec", energie := "Electricité"]
prix_energie[variable == "prix_gaz", energie := "Gaz"]
prix_energie[variable == "prix_fioul", energie := "Fioul"]
prix_energie[variable == "prix_urbain", energie := "Urbain"]
prix_energie[variable == "prix_autres", energie := "Autres"]
prix_energie[variable == "CCE", energie := "CCE"]


prix_energie =dcast.data.table(prix_energie, annee~energie)

##### conso du parc initial 

besoins_parc_init

###### coûts des gestes 
cout_geste_tmp = cout_geste[,list(COD_PERIODE_SIMPLE,COD_PERIODE_DETAIL,
                                  COD_BAT_TYPE,COD_SS_BRANCHE,COD_BRANCHE,
                                 GESTE, GAIN, COUT,CEE)]


cout_geste_tmp[, ID_merge := paste0(COD_BRANCHE,COD_SS_BRANCHE,COD_BAT_TYPE, COD_PERIODE_DETAIL,
                                          COD_PERIODE_SIMPLE),]

cout_geste_tmp = melt(cout_geste_tmp[,list( ID_merge,GESTE, GAIN, COUT,CEE)], id.vars = c("ID_merge", "GESTE"))


cout_geste_casted = dcast.data.table(cout_geste_tmp,  ID_merge  ~ variable + GESTE )

besoins_parc_init[,ID_merge := paste0(COD_BRANCHE,COD_SS_BRANCHE,COD_BAT_TYPE, COD_PERIODE_DETAIL,
                                   COD_PERIODE_SIMPLE)]


listgeste = c("ENSBBC","FENBBC","FEN_MURBBC","ENSMOD","FENMOD","FEN_MURMOD","GTB")

##### durée de vie des gestes 
DVgeste  =c(50,20,20,50,20,20,10) 


###### coûts des systèmes de chauffage
cout_rdt_chauff_tmp = cout_rdt_chauff[PERIODE == "1",list(COD_BAT_TYPE,COD_SS_BRANCHE,COD_BRANCHE,
                                                          COD_SYSTEME_CHAUD,COD_ENERGIE, RDT, COUT,CEE)]

cout_rdt_chauff_tmp[, ID_merge_chauff := paste0(COD_BRANCHE,COD_SS_BRANCHE,COD_BAT_TYPE),]

cout_rdt_chauff_tmp = melt(cout_rdt_chauff_tmp[,list( ID_merge_chauff,COD_SYSTEME_CHAUD,COD_ENERGIE, RDT, COUT,CEE)], 
                           id.vars = c("ID_merge_chauff", "COD_SYSTEME_CHAUD", "COD_ENERGIE"))

cout_rdt_chauff_casted = dcast.data.table(cout_rdt_chauff_tmp,  ID_merge_chauff  ~ variable +COD_SYSTEME_CHAUD+COD_ENERGIE )
#cout_rdt_chauff_casted = dcast.data.table(cout_rdt_chauff_tmp,  ID_merge_chauff +COD_SYSTEME_CHAUD+COD_ENERGIE ~ variable )

besoins_parc_init[,ID_merge_chauff :=paste0(COD_BRANCHE,COD_SS_BRANCHE,COD_BAT_TYPE)]

liste_systchaud = unique(cout_rdt_chauff_tmp[,list(COD_SYSTEME_CHAUD, COD_ENERGIE)])[order(COD_SYSTEME_CHAUD), paste(COD_SYSTEME_CHAUD, 
                                                                                                                     COD_ENERGIE,sep="_")]
##### energie du nouveau système 
energie_syst_chaud = unique(cout_rdt_chauff_tmp[,list(COD_SYSTEME_CHAUD, COD_ENERGIE)])[order(COD_SYSTEME_CHAUD),
                                                                                        list(COD_SYST_ENER = paste(COD_SYSTEME_CHAUD,COD_ENERGIE,sep="_"),
                                                                                             COD_ENERGIE)]   
energie_syst_chaud = merge(energie_syst_chaud,COD_ENERGIE, by="COD_ENERGIE")

###### durée de vie des systèmes de chauffage 

DV_systchaud = unique(cout_intan_chauff[,list(COD_SYSTEME_CHAUD, COD_ENERGIE,DUREE_VIE)])[order(COD_SYSTEME_CHAUD), 
                                                                           list(COD_SYST_ENER = paste(COD_SYSTEME_CHAUD,  
                                                                                                      COD_ENERGIE,sep="_"),DV =DUREE_VIE) ]
                                                                                
                                                                                
     



##### ##### ##### ##### ##### ##### ##### ##### 
##### calcul rentabilité des gestes 
##### ##### ##### ##### ##### ##### ##### ##### 

calc_renta = merge(besoins_parc_init, cout_geste_casted, by=c("ID_merge"), all.x=T)

### taux d'acutalisation 
calc_renta[COD_OCCUPANT == "05", r_actu := 0.07]
calc_renta[COD_OCCUPANT != "05", r_actu := 0.04]

#calc_renta = melt(calc_renta[,list(ID,CONSO_CHAUFF,GAIN_ENSBBC)], id.vars = )

VAN_geste = function(CINV,gain_conso, DV, energie, r){
  VAN = -CINV
  for(t in 1:DV){
  VAN = VAN + gain_conso*prix_energie[,energie,with=F][t]/(1+r)^t
  }
  return(VAN)
}

VAN_geste2 = function(CINV,gain_conso, DV, p_e, r){
  VAN = -CINV
  ### version anticipation parfaite
#  for(t in 1:DV){
#    VAN = VAN + gain_conso*p_e[t]/(1+r)^t
#  }
  
  ### version myope
  for(t in 1:DV){
    VAN = VAN + gain_conso*p_e[1]/(1+r)^t
  }
  return(VAN)
}


### calcul VAN sur tout le parc pour tous les gestes, sur la durée de vie du geste ######

i=0
for(geste_tmp in listgeste){
  i = i + 1
  DV = DVgeste[i]
  calc_renta[,paste0("GAINCONSOCHAUF_", geste_tmp) := get(paste0("GAIN_", geste_tmp))*(CONSO_CHAUFF+BESOIN_AUXILIAIRES_CALC+BESOIN_VENTILATION)]
#  calc_renta[,paste0("GAINCONSOCHAUF_", geste_tmp) := get(paste0("GAIN_", geste_tmp))*(CONSO_CHAUFF)]
  calc_renta[,paste0("COUTTOT_", geste_tmp) := get(paste0("COUT_", geste_tmp))*SURFACES]
#  calc_renta[,paste0("VAN", geste_tmp) := VAN_geste(get(paste0("COUT_TOT", geste_tmp)), get(paste0("GAIN_CONSO_CHAUF_", geste_tmp)),
#                                             DV, energie = ENERGIE,r ),
#             by="ID"]

  calc_renta[ENERGIE == "Electricité",
             paste0("VAN_", geste_tmp) := VAN_geste2(get(paste0("COUTTOT_", geste_tmp)), get(paste0("GAINCONSOCHAUF_", geste_tmp)),
                                             DV,prix_energie$Electricité ,r_actu ),
             by="ID"]
  
  calc_renta[ENERGIE == "Gaz",
             paste0("VAN_", geste_tmp) := VAN_geste2(get(paste0("COUTTOT_", geste_tmp)), get(paste0("GAINCONSOCHAUF_", geste_tmp)),
                                             DV,prix_energie$Gaz ,r_actu),
             by="ID"]
  calc_renta[ENERGIE == "Fioul",
             paste0("VAN_", geste_tmp) := VAN_geste2(get(paste0("COUTTOT_", geste_tmp)), get(paste0("GAINCONSOCHAUF_", geste_tmp)),
                                             DV,prix_energie$Fioul ,r_actu ),
             by="ID"]
  calc_renta[ENERGIE == "Urbain",
             paste0("VAN_", geste_tmp) := VAN_geste2(get(paste0("COUTTOT_", geste_tmp)), get(paste0("GAINCONSOCHAUF_", geste_tmp)),
                                             DV,prix_energie$Urbain ,r_actu ),
             by="ID"]
  calc_renta[ENERGIE == "Autres",
             paste0("VAN_", geste_tmp) := VAN_geste2(get(paste0("COUTTOT_", geste_tmp)), get(paste0("GAINCONSOCHAUF_", geste_tmp)),
                                             DV,prix_energie$Autres,r_actu ),
             by="ID"]

}

summary(calc_renta)


coltokeep = c("BRANCHE","ID", "ENERGIE", "COD_PERIODE_SIMPLE", "SURFACES","CONSO_CHAUFF",paste0("GAIN_",listgeste ), paste0("VAN_",listgeste ), paste0("COUTTOT_", listgeste), 
              paste0("GAINCONSOCHAUF_", listgeste))


renta_geste = melt(calc_renta[, coltokeep, with=F], id.vars = c("BRANCHE","ID", "ENERGIE", 
                                                                "COD_PERIODE_SIMPLE", "SURFACES","CONSO_CHAUFF"))

renta_geste[,var := sapply(strsplit(as.character(variable), split = "_"),"[", 1),]
renta_geste[,geste1 := sapply(strsplit(as.character(variable), split = "_"),"[", 2),]
renta_geste[,geste2 := sapply(strsplit(as.character(variable), split = "_"),"[", 3),]
renta_geste[!is.na(geste2) ,geste := paste(geste1,geste2,sep="_"), by="ID"]
renta_geste[is.na(geste2) ,geste := geste1, by="ID"]

renta_geste = dcast.data.table(renta_geste, ID + BRANCHE + ENERGIE + COD_PERIODE_SIMPLE + SURFACES +CONSO_CHAUFF+ geste ~ var )


###### distribution des VANS par type de geste

ggplot(renta_geste[!is.na(VAN)]) + geom_boxplot(aes(geste,VAN/10^9)) + mytheme_facet_plot 

ggplot(renta_geste[!is.na(VAN)]) + geom_point(aes(geste,VAN/10^9)) + mytheme_facet_plot + facet_wrap(~BRANCHE, ncol = 2)

ggplot(renta_geste[!is.na(VAN)]) + geom_point(aes(geste,VAN/COUTTOT)) + mytheme_facet_plot + facet_wrap(~ENERGIE, ncol = 2)
ggplot(renta_geste[!is.na(VAN)]) + geom_point(aes(geste,VAN/COUTTOT, color = ENERGIE)) + mytheme_facet_plot 


quantile(renta_geste [, VAN], na.rm = T)

###### gain de conso et investissements par les options les plus rentables pour chaque partie du parc  

renta_geste[, VAN_max := max(VAN, na.rm = T), by = "ID"]
renta_geste[, VANsurI := VAN/COUTTOT, by = "ID"]
renta_geste[, VANsurI_max := max(VAN/COUTTOT, na.rm = T), by = "ID"]

renta_geste[VAN == VAN_max & VAN > 0,.N,by="geste"]
renta_geste[VAN == VAN_max & VAN > 0,sum(SURFACES)/10^6]
renta_geste[VAN == VAN_max & VAN > 0,sum(SURFACES)/10^6,by="geste"]
renta_geste[VAN == VAN_max & VAN > 0,sum(COUTTOT)/10^9]
renta_geste[VAN == VAN_max & VAN > 0,sum(GAINCONSOCHAUF)/10^9]

recap_renta_geste  = renta_geste[VAN/COUTTOT ==  VANsurI_max & VAN > 0,list(Surfaces_renov_Mm2 = sum(SURFACES)/10^6,
                                                               CINV_Meuros = sum(COUTTOT)/10^9,
                                                               GAINCONSO_tWh = sum(GAINCONSOCHAUF)/10^9), by="geste"]


recap_renta_geste_tot = renta_geste[VAN/COUTTOT==  VANsurI_max & VAN > 0,list(geste = "Total rénovation",
                                                                  Surfaces_renov_Mm2 = sum(SURFACES)/10^6,
                                                                  CINV_Meuros = sum(COUTTOT)/10^9,
                                                                  GAINCONSO_tWh = sum(GAINCONSOCHAUF)/10^9)]


recap_renta_AME = COUTS[scenario == "AME prix MA3" & annee == "2010" & typeRenovationBatiment != "Etat initial", 
                        list(geste = "Total rénovation AME",
                             Surfaces_renov_Mm2 = sum(surface)/10^6,
                             CINV_Meuros  = sum(investissement)/10^9,
                             GAINCONSO_tWh = NA)]
  
recap_renta_AME2 = COUTS[scenario == "AME prix MA3 valeur verte" & annee == "2010" & typeRenovationBatiment != "Etat initial", 
                        list(geste = "Total sans coûts intangibles",
                             Surfaces_renov_Mm2 = sum(surface)/10^6,
                             CINV_Meuros  = sum(investissement)/10^9,
                             GAINCONSO_tWh = NA)]

rbind(recap_renta_geste, recap_renta_geste_tot, recap_renta_AME, recap_renta_AME2)

COUTS[scenario == "AME prix MA3 valeur verte" & annee == "2010" & typeRenovationBatiment != "Etat initial", sum(investissement)/10^9]
COUTS[scenario == "AME prix MA3 valeur verte" & annee == "2010" & typeRenovationBatiment != "Etat initial", sum(surface)/10^6]

####### calc proba de changement #####
nu=8
renta_geste[, sum_CG := sum((VAN+2*COUTTOT)^(-nu), na.rm=T), by = "ID"]
renta_geste[, Pc := (VAN+2*COUTTOT)^(-nu)/sum_CG, by = "ID"]

summary(renta_geste)

summary(renta_geste$Pc)

renta_geste[ID == "021821051102100102",]
renta_geste[ID == "061734010103060102",]

calc_renta[ID == "061734010103060102",]


renta_geste[Pc > 0.1 & Pc < 0.8]


###### verif sur les segements où la VAN est très élevée

renta_geste[VANsurI > 10]
calc_renta[VAN_ENSMOD/COUTTOT_ENSMOD > 10]
calc_renta[VAN_ENSMOD/COUTTOT_ENSMOD > 10, 
           list(S = SURFACES/10^6,CONSO = CONSO_CHAUFF/10^9, ENERGIE, r_actu,
                COUT_ENSMOD, GAIN_ENSMOD,CINV = COUTTOT_ENSMOD/10^6,
                GAIN_CONSO = GAINCONSOCHAUF_ENSMOD/10^9, VAN = VAN_ENSMOD/10^6, VANsurI=VAN_ENSMOD/COUTTOT_ENSMOD)]

calc_renta[VAN_ENSMOD/COUTTOT_ENSMOD > 10, 
           list(S = SURFACES/10^6,CONSO = CONSO_CHAUFF/10^9, ENERGIE, r_actu,
                COUT_ENSMOD, GAIN_ENSMOD,CINV = COUTTOT_ENSMOD/10^6,
                GAIN_CONSO = GAINCONSOCHAUF_ENSMOD/10^9, VAN_ENSMOD = VAN_ENSMOD/10^6, VANsurI_ENSMOD=VAN_ENSMOD/COUTTOT_ENSMOD)]


calc_renta[VAN_ENSMOD/COUTTOT_ENSMOD > 10, 
           list(S = SURFACES/10^6,CONSO = CONSO_CHAUFF/10^9, ENERGIE, r_actu,
                COUT_GTB, GAIN_GTB,CINV_GTB = COUTTOT_GTB/10^6,
                GAIN_CONSO_GTB = GAINCONSOCHAUF_GTB/10^9, VAN_GTB = VAN_GTB/10^6, VANsurI_GTB=VAN_GTB/COUTTOT_GTB)]

calc_renta[VAN_ENSMOD/COUTTOT_ENSMOD > 10, 
           list(S = SURFACES/10^6,CONSO = CONSO_CHAUFF/10^9, ENERGIE, r_actu,
                COUT_FENMOD, GAIN_FENMOD,CINV_FENMOD = COUTTOT_FENMOD/10^6,
                GAIN_CONSO_FENMOD = GAINCONSOCHAUF_FENMOD/10^9, VAN_FENMOD = VAN_FENMOD/10^6, VANsurI_FENMOD=VAN_FENMOD/COUTTOT_FENMOD)]

###### fonctions VAN test ######

energie = "Electricité"
DV = 25
CINV = calc_renta[1,get(paste0("COUTTOT_", geste_tmp))]
gain_conso =  calc_renta[1,get(paste0("GAINCONSOCHAUF_", geste_tmp))]
r=0.04

VAN_geste(CINV, gain_conso, DV,energie ,r )
VAN_geste2(CINV, gain_conso, DV,prix_energie$Electricité ,r )

##### ##### ##### ##### ##### ##### ##### ##### ##### ##### #####
##### calcul rentabilité des changements de systèmes de chauffage
##### ##### ##### ##### ##### ##### ##### ##### ##### ##### #####

calc_renta_chauff = merge(besoins_parc_init, cout_rdt_chauff_casted, by=c("ID_merge_chauff"), all.x=T)

unique(cout_rdt_chauff_casted$ID_merge_chauff)
besoins_parc_init[ID_merge_chauff %in%unique(cout_rdt_chauff_casted$ID_merge_chauff)]
                    
summary(calc_renta_chauff$CONSO_CHAUFF)

### taux d'acutalisation 
calc_renta_chauff[COD_OCCUPANT == "05", r_actu := 0.07]
calc_renta_chauff[COD_OCCUPANT != "05", r_actu := 0.04]

VAN_chgt_syst =  function(CINV,Conso_ini, DV, p_e_ini, p_e_fin, rdt_ini, rdt_fin, r){
  VAN = -CINV
  
#  for(t in 1:DV){
#    VAN = VAN + Conso_ini*(p_e_ini[t] - p_e_fin[t]*rdt_ini/rdt_fin)/(1+r)^t
#  }
  #### version myope
  for(t in 1:DV){
    VAN = VAN + Conso_ini*(p_e_ini[1] - p_e_fin[1]*rdt_ini/rdt_fin)/(1+r)^t
  }
  return(VAN)
}

### COUT DE RENOUVELLEMENT DU SYSTEME ACTUEL DE CHAUFFAGE 
calc_renta_chauff[,COUT_SYST_ACTUEL := COUT*SURFACES]



### calcul VAN sur tout le parc pour tous les gestes, sur la durée de vie du geste ######

i=0
for(syst_tmp in liste_systchaud){
  i = i + 1
  DV = DV_systchaud[COD_SYST_ENER == syst_tmp,DV]
  energie_fin = energie_syst_chaud[COD_SYST_ENER == syst_tmp,ENERGIE]
  p_e_fin = prix_energie[,get(energie_fin)]
  
  
  
  calc_renta_chauff[,paste0("GAINCONSOCHAUF_", syst_tmp) := (1-RDT/get(paste0("RDT_", syst_tmp)))*CONSO_CHAUFF]
  
  calc_renta_chauff[,paste0("COUTTOT_",syst_tmp) := get(paste0("COUT_", syst_tmp))*SURFACES]
  calc_renta_chauff[,paste0("SURCOUT_",syst_tmp) := get(paste0("COUT_", syst_tmp))*SURFACES - COUT_SYST_ACTUEL]
#calc_renta_chauff[,paste0("SURCOUT_",syst_tmp) := get(paste0("COUT_", syst_tmp))*SURFACES]
  
  
  calc_renta_chauff[ENERGIE == "Electricité",
             paste0("VAN_", syst_tmp) := VAN_chgt_syst(CINV = get(paste0("SURCOUT_", syst_tmp)), Conso_ini = CONSO_CHAUFF, DV = DV, 
                                                       rdt_ini = RDT, rdt_fin = get(paste0("RDT_", syst_tmp)),
                                                       p_e_ini = prix_energie$Electricité, p_e_fin =  p_e_fin,r = r_actu),
             by="ID"]
  
  calc_renta_chauff[ENERGIE == "Gaz",
                    paste0("VAN_", syst_tmp) := VAN_chgt_syst(CINV = get(paste0("SURCOUT_", syst_tmp)), Conso_ini = CONSO_CHAUFF, DV = DV, 
                                                              rdt_ini = RDT, rdt_fin = get(paste0("RDT_", syst_tmp)),
                                                              p_e_ini = prix_energie$Gaz, p_e_fin =  p_e_fin,r = r_actu),
                    by="ID"]
  
  calc_renta_chauff[ENERGIE == "Fioul",
                    paste0("VAN_", syst_tmp) := VAN_chgt_syst(CINV = get(paste0("SURCOUT_", syst_tmp)), Conso_ini = CONSO_CHAUFF, DV = DV, 
                                                              rdt_ini = RDT, rdt_fin = get(paste0("RDT_", syst_tmp)),
                                                              p_e_ini = prix_energie$Fioul, p_e_fin =  p_e_fin,r = r_actu),
                    by="ID"]

  calc_renta_chauff[ENERGIE == "Urbain",
                    paste0("VAN_", syst_tmp) := VAN_chgt_syst(CINV = get(paste0("SURCOUT_", syst_tmp)), Conso_ini = CONSO_CHAUFF, DV = DV, 
                                                              rdt_ini = RDT, rdt_fin = get(paste0("RDT_", syst_tmp)),
                                                              p_e_ini = prix_energie$Urbain, p_e_fin =  p_e_fin,r = r_actu),
                    by="ID"]
  
  calc_renta_chauff[ENERGIE == "Autres",
                    paste0("VAN_", syst_tmp) := VAN_chgt_syst(CINV = get(paste0("SURCOUT_", syst_tmp)), Conso_ini = CONSO_CHAUFF, DV = DV, 
                                                              rdt_ini = RDT, rdt_fin = get(paste0("RDT_", syst_tmp)),
                                                              p_e_ini = prix_energie$Autres, p_e_fin =  p_e_fin,r = r_actu),
                    by="ID"]
  
}


coltokeep = c("BRANCHE","ID", "ENERGIE","SYSTEME_CHAUD","COD_PERIODE_SIMPLE", "SURFACES","CONSO_CHAUFF",
              "RDT","COUT_SYST_ACTUEL",
              paste0("RDT_",liste_systchaud ), 
              paste0("VAN_",liste_systchaud ), 
              paste0("COUTTOT_", liste_systchaud), 
              paste0("GAINCONSOCHAUF_", liste_systchaud))


renta_syst = melt(calc_renta_chauff[, coltokeep, with=F], id.vars = c("BRANCHE","ID", "ENERGIE","SYSTEME_CHAUD", 
                                                                "COD_PERIODE_SIMPLE", "SURFACES", "CONSO_CHAUFF", 
                                                                "RDT","COUT_SYST_ACTUEL"))

setnames(renta_syst, "RDT","RDT_ACTUEL")

renta_syst[,var := sapply(strsplit(as.character(variable), split = "_"),"[", 1),]
renta_syst[,COD_SYSTEME_CHAUD_FIN := sapply(strsplit(as.character(variable), split = "_"),"[", 2),]
renta_syst[,COD_ENERGIE_FIN := sapply(strsplit(as.character(variable), split = "_"),"[", 3),]
renta_syst = merge(renta_syst, COD_SYSTEME_CHAUD[, list(COD_SYSTEME_CHAUD_FIN = COD_SYSTEME_CHAUD, 
                                                        SYSTEME_CHAUD_FIN =  SYSTEME_CHAUD)],by= "COD_SYSTEME_CHAUD_FIN", all.x=T)

renta_syst = merge(renta_syst, COD_ENERGIE[, list(COD_ENERGIE_FIN = COD_ENERGIE, 
                                                        ENERGIE_FIN =  ENERGIE)],by= "COD_ENERGIE_FIN", all.x=T)

renta_syst[,COD_SYST_ENER := paste(COD_SYSTEME_CHAUD_FIN,COD_ENERGIE_FIN,sep="_"), by="ID"]


renta_syst = dcast.data.table(renta_syst, ID + BRANCHE + ENERGIE+SYSTEME_CHAUD+ COD_PERIODE_SIMPLE + SURFACES +
                                CONSO_CHAUFF+ RDT_ACTUEL + COUT_SYST_ACTUEL +  COD_SYST_ENER + ENERGIE_FIN + SYSTEME_CHAUD_FIN  ~ var )

renta_syst[,SYSTEME_CHAUD_FIN := factor(SYSTEME_CHAUD_FIN,levels=c("Chaudière gaz","Chaudière condensation gaz",
                                                           "Chaudière fioul","Chaudière condensation fioul",
                                                           "Electrique direct","Electrique direct performant",
                                                           "PAC","PAC performant",
                                                           "Rooftop", "Rooftop performant",
                                                           "Tube radiant", "Tube radiant performant" ,
                                                           "Cassette rayonnante","Cassette rayonnante performant",
                                                           "DRV", "DRV performant",
                                                           "Autre système centralisé",  "Autre système centralisé performant",
                                                           "nr"))]

nom_syst_non_performant =c("Chaudière gaz", "Chaudière fioul", "Electrique direct", "PAC",
                           "Rooftop", "Tube radiant", "Cassette rayonnante","DRV", 
                           "Autre système centralisé")

###### distribution des VANS par système de chauffage

ggplot(renta_syst[!is.na(VAN)]) + geom_boxplot(aes(COD_SYST_ENER,VAN/10^9)) + mytheme_facet_plot + 
  theme(axis.text.x = element_text(angle = 45, hjust = 1))

ggplot(renta_syst[!is.na(VAN)]) + geom_boxplot(aes(SYSTEME_CHAUD_FIN,VAN/10^9)) + mytheme_facet_plot + 
  theme(axis.text.x = element_text(angle = 45, hjust = 1))

ggplot(renta_syst[!is.na(VAN)]) + geom_boxplot(aes(SYSTEME_CHAUD_FIN,VAN/10^9)) + mytheme_facet_plot + 
  theme(axis.text.x = element_text(angle = 45, hjust = 1)) + facet_wrap(~BRANCHE, ncol = 2)


###### gain de conso et investissements par les options les plus rentables pour chaque partie du parc  

renta_syst[, VAN_max := max(VAN, na.rm = T), by = "ID"]
renta_syst[, VANsurI_max := max(VAN/COUTTOT, na.rm = T), by = "ID"]

renta_syst[VAN == VAN_max & VAN > 0,.N,by="COD_SYST_ENER"]
renta_syst[VAN == VAN_max & VAN > 0,.N,by="SYSTEME_CHAUD_FIN"]

renta_syst[VAN == VAN_max & VAN > 0,sum(SURFACES)/10^6]
renta_syst[VAN == VAN_max & VAN > 0,sum(SURFACES)/10^6,by="COD_SYST_ENER"]
renta_syst[VAN == VAN_max & VAN > 0,sum(COUTTOT)/10^9]
renta_syst[VAN == VAN_max & VAN > 0,sum(GAINCONSOCHAUF)/10^9]

COUTS[scenario == "AME prix MA3" & annee == "2010" & typeRenovationSysteme != "Etat initial_NP", sum(investissement)/10^9]
COUTS[scenario == "AME prix MA3" & annee == "2010" & typeRenovationBatiment != "Etat initial_NP", sum(surface)/10^6]

COUTS[scenario == "AME prix MA3 valeur verte" & annee == "2010" & typeRenovationSysteme != "Etat initial_NP", sum(investissement)/10^9]
COUTS[scenario == "AME prix MA3 valeur verte" & annee == "2010" & typeRenovationBatiment != "Etat initial_NP", sum(surface)/10^6]


recap_renta_syst  = renta_syst[VAN/COUTTOT ==  VANsurI_max & VAN > 0,list(Surfaces_renov_Mm2 = sum(SURFACES)/10^6,
                                                               CINV_Meuros = sum(COUTTOT)/10^9,
                                                               GAINCONSO_tWh = sum(GAINCONSOCHAUF)/10^9), by="SYSTEME_CHAUD_FIN"]

renta_syst[VAN/COUTTOT ==  VANsurI_max & VAN > 0 & GAINCONSOCHAUF < 0]

recap_renta_syst  = renta_syst[VAN/COUTTOT ==  VANsurI_max & VAN > 0 &  GAINCONSOCHAUF>0,
                               list(Surfaces_renov_Mm2 = sum(SURFACES)/10^6,
                                                                          CINV_Meuros = sum(COUTTOT)/10^9,
                                                                          GAINCONSO_tWh = sum(GAINCONSOCHAUF)/10^9), 
                               by=c("ENERGIE","SYSTEME_CHAUD","ENERGIE_FIN","SYSTEME_CHAUD_FIN")]

recap_renta_syst[order(SYSTEME_CHAUD)]

recap_renta_syst_tot = renta_syst[VAN/COUTTOT==  VANsurI_max & VAN > 0  &  GAINCONSOCHAUF>0, list(ENERGIE = "",SYSTEME_CHAUD = "",  ENERGIE_FIN = "",
                                                                  SYSTEME_CHAUD_FIN = "Total",
                                                                  Surfaces_renov_Mm2 = sum(SURFACES)/10^6,
                                                                  CINV_Meuros = sum(COUTTOT)/10^9,
                                                                  GAINCONSO_tWh = sum(GAINCONSOCHAUF)/10^9)]


recap_renta_syst_AME = COUTS[scenario == "AME prix MA3" & annee == "2010" & typeRenovationSysteme != "Etat initial_NP", 
                        list(ENERGIE = "",SYSTEME_CHAUD = "",  ENERGIE_FIN = "",
                             SYSTEME_CHAUD_FIN = "AME",
                             
                             Surfaces_renov_Mm2 = sum(surface)/10^6,
                             CINV_Meuros  = sum(investissement)/10^9,
                             GAINCONSO_tWh = NA)]

recap_renta_syst_AME2 = COUTS[scenario == "AME prix MA3 valeur verte" & annee == "2010" & typeRenovationSysteme != "Etat initial_NP", 
                         list(ENERGIE = "",SYSTEME_CHAUD = "",  ENERGIE_FIN = "",
                              SYSTEME_CHAUD_FIN = "Sans CINT",
                              Surfaces_renov_Mm2 = sum(surface)/10^6,
                              CINV_Meuros  = sum(investissement)/10^9,
                              GAINCONSO_tWh = NA)]

recap_renta_syst[, ENERGIE := factor(ENERGIE,levels = c("Electricité","Gaz","Fioul","Urbain","Autres"), labels  = c("Elec","Gaz","Fioul","Urbain","Autres"))]
recap_renta_syst[, ENERGIE_FIN := factor(ENERGIE_FIN, levels = c("Electricité","Gaz","Fioul","Urbain","Autres"), 
                                         labels  = c("Elec","Gaz","Fioul","Urbain","Autres"))]

recap_renta_syst[, SYSTEME_CHAUD := factor(SYSTEME_CHAUD, levels = c("Chaudière gaz","Chaudière condensation gaz",
                                                                     "Chaudière fioul","Chaudière condensation fioul",
                                                                     "Electrique direct","Electrique direct performant",
                                                                     "PAC","PAC performant",
                                                                     "Rooftop", "Rooftop performant",
                                                                     "Tube radiant", "Tube radiant performant" ,
                                                                     "Cassette rayonnante","Cassette rayonnante performant",
                                                                     "DRV", "DRV performant",
                                                                     "Autre système centralisé",  "Autre système centralisé performant",
                                                                     "nr"), 
                                                      labels = c("Chaud Gaz","Cond Gaz","Chaud fioul","Cond fioul",
                                                                 "Elec dir","Elec dir perf","PAC","PAC perf","Tube","Tube perf",
                                                                 "Cass", "Cass perf","DRV","DRV perf",
                                                                 "Rooftop","Rooftop perf","Au central","Au central perf","nr"))]

recap_renta_syst[, SYSTEME_CHAUD_FIN := factor(SYSTEME_CHAUD_FIN, levels = c("Chaudière gaz","Chaudière condensation gaz",
                                                                     "Chaudière fioul","Chaudière condensation fioul",
                                                                     "Electrique direct","Electrique direct performant",
                                                                     "PAC","PAC performant",
                                                                     "Rooftop", "Rooftop performant",
                                                                     "Tube radiant", "Tube radiant performant" ,
                                                                     "Cassette rayonnante","Cassette rayonnante performant",
                                                                     "DRV", "DRV performant",
                                                                     "Autre système centralisé",  "Autre système centralisé performant",
                                                                     "nr"), 
                                           labels = c("Chaud Gaz","Cond Gaz","Chaud fioul","Cond fioul",
                                                      "Elec dir","Elec dir perf","PAC","PAC perf","Tube","Tube perf",
                                                      "Cass", "Cass perf","DRV","DRV perf",
                                                      "Rooftop","Rooftop perf","Au central","Au central perf","nr"))]

rbind(recap_renta_syst, recap_renta_syst_tot, recap_renta_syst_AME, recap_renta_syst_AME2)




COUTS[scenario == "AME prix MA3 valeur verte" & annee == "2010" & typeRenovationSysteme != "Etat initial_NP", 
      list(SYSTEME_CHAUD_FIN = "Total sans coûts intangibles",
           Surfaces_renov_Mm2 = sum(surface)/10^6,
           CINV_Meuros  = sum(investissement)/10^9,
           GAINCONSO_tWh = NA), by="typeRenovationSysteme"][order( CINV_Meuros)]

####### calc proba de changement #####
nu=8
renta_syst[, sum_CG := sum((VAN+2*COUTTOT)^(-nu), na.rm=T), by = "ID"]
renta_syst[, Pc := (VAN+2*COUTTOT)^(-nu)/sum_CG, by = "ID"]

summary(renta_syst$Pc)

renta_geste[Pc > 0.1 & Pc < 0.8]


#### coûts moyens des gestes et des systèmes pondérés par les surfaces du parc #####
cout_geste[GESTE == "GTB", COUT]
renta_geste[geste =="GTB",  COUTTOT/SURFACES]
cout_moy_geste = renta_geste[!is.na(COUTTOT) & ! is.na(SURFACES), sum(COUTTOT, na.rm=T)/sum(SURFACES, na.rm = T), by=c("geste", "BRANCHE")]
cout_moy_geste = dcast.data.table(cout_moy_geste, BRANCHE ~ geste)
cout_moy_geste =  rbind(cout_moy_geste, 
                        dcast.data.table(renta_geste[!is.na(COUTTOT) & ! is.na(SURFACES), list(BRANCHE = "Ensemble",sum(COUTTOT, na.rm=T)/sum(SURFACES, na.rm = T)), by="geste"], BRANCHE ~ geste))


cout_moy_syst = renta_syst[!is.na(COUTTOT) & ! is.na(SURFACES), sum(COUTTOT, na.rm=T)/sum(SURFACES, na.rm = T), by=c("SYSTEME_CHAUD_FIN", "BRANCHE")]
cout_moy_syst = dcast.data.table(cout_moy_syst , BRANCHE ~ SYSTEME_CHAUD_FIN)
cout_moy_syst =  rbind(cout_moy_syst, 
                        dcast.data.table(renta_syst[!is.na(COUTTOT) & ! is.na(SURFACES), list(BRANCHE = "Ensemble",sum(COUTTOT, na.rm=T)/sum(SURFACES, na.rm = T)), 
                                                    by="SYSTEME_CHAUD_FIN"], BRANCHE ~ SYSTEME_CHAUD_FIN))

#### gainsmoyens des gestes et des systèmes pondérés par les surfaces du parc #####


gain_moy_geste = renta_geste[!is.na(GAINCONSOCHAUF) & ! is.na(SURFACES), sum(GAINCONSOCHAUF, na.rm=T)/sum(SURFACES, na.rm = T), by=c("geste", "BRANCHE")]
gain_moy_geste = dcast.data.table(gain_moy_geste, BRANCHE ~ geste)
gain_moy_geste =  rbind(gain_moy_geste, 
                        dcast.data.table(renta_geste[!is.na(GAINCONSOCHAUF) & ! is.na(SURFACES), list(BRANCHE = "Ensemble",sum(GAINCONSOCHAUF, na.rm=T)/sum(SURFACES, na.rm = T)), by="geste"], BRANCHE ~ geste))


gain_moy_syst = renta_syst[!is.na(GAINCONSOCHAUF) & ! is.na(SURFACES), sum(GAINCONSOCHAUF, na.rm=T)/sum(SURFACES, na.rm = T), by=c("SYSTEME_CHAUD_FIN", "BRANCHE")]
gain_moy_syst = dcast.data.table(gain_moy_syst , BRANCHE ~ SYSTEME_CHAUD_FIN)
gain_moy_syst =  rbind(gain_moy_syst, 
                       dcast.data.table(renta_syst[!is.na(GAINCONSOCHAUF) & ! is.na(SURFACES), list(BRANCHE = "Ensemble",sum(GAINCONSOCHAUF, na.rm=T)/sum(SURFACES, na.rm = T)), 
                                                   by="SYSTEME_CHAUD_FIN"], BRANCHE ~ SYSTEME_CHAUD_FIN))

#### cout du kWh des gestes et des systèmes pondérés par les surfaces du parc #####

coutkWh_moy_geste = renta_geste[!is.na(GAINCONSOCHAUF) & ! is.na(COUTTOT), sum(COUTTOT, na.rm = T)/sum(GAINCONSOCHAUF, na.rm=T), 
                                by=c("geste", "BRANCHE")]
coutkWh_moy_geste= dcast.data.table(coutkWh_moy_geste  , BRANCHE ~ geste)
coutkWh_moy_geste=  rbind(coutkWh_moy_geste, 
                        dcast.data.table(renta_geste[!is.na(GAINCONSOCHAUF) & ! is.na(COUTTOT), list(BRANCHE = "Ensemble",sum(COUTTOT, na.rm = T)/sum(GAINCONSOCHAUF, na.rm=T)), by="geste"], BRANCHE ~ geste))


coutkWh_moy_syst = renta_syst[!is.na(GAINCONSOCHAUF) & ! is.na(COUTTOT), sum(COUTTOT, na.rm = T)/sum(GAINCONSOCHAUF, na.rm=T), by=c("SYSTEME_CHAUD_FIN", "BRANCHE")]
coutkWh_moy_syst = dcast.data.table(coutkWh_moy_syst, BRANCHE ~ SYSTEME_CHAUD_FIN)
coutkWh_moy_syst =  rbind(coutkWh_moy_syst, 
                       dcast.data.table(renta_syst[!is.na(GAINCONSOCHAUF) & ! is.na(COUTTOT), list(BRANCHE = "Ensemble",sum(COUTTOT, na.rm = T)/sum(GAINCONSOCHAUF, na.rm=T)), 
                                                   by="SYSTEME_CHAUD_FIN"], BRANCHE ~ SYSTEME_CHAUD_FIN))


##### tableaux recapitulatif des coûts 
DVgeste
DV_systchaud

tableau_cout_moy_geste = renta_geste[!is.na(COUTTOT) & ! is.na(SURFACES), 
            list("Coûts moyens investisstement (euros par m²)" = sum(COUTTOT, na.rm=T)/sum(SURFACES, na.rm = T),
                 "Gains moyens (en % de la consommation initiale)" = weighted.mean(GAIN,w = SURFACES , na.rm = T),
                   "Gains moyens (kwEF par m² par an)" = sum(GAINCONSOCHAUF, na.rm=T)/sum(SURFACES, na.rm = T),
                   "Coût d'investissemet pour 1 kWh (euros par kwEF par an" = sum(COUTTOT, na.rm = T)/sum(GAINCONSOCHAUF, na.rm=T),
                 "Gains moyens (en % de la consommation initiale de chauffage + 32 kWh)" =
                   weighted.mean(GAINCONSOCHAUF/(CONSO_CHAUFF +32*SURFACES), na.rm=T,w=SURFACES)),
            by="geste"]
tableau_cout_moy_geste[, "Durée de vie" := c(50,50,20,20,20,20,10)]



tableau_cout_moy_geste[, geste := factor(geste, levels = c("GTB", "FENMOD", "FENBBC", "FEN_MURMOD", "FEN_MURBBC", "ENSMOD", "ENSBBC"))]
tableau_cout_moy_geste = tableau_cout_moy_geste[order(geste)]

tableau_cout_moy_syst = renta_syst[!is.na(COUTTOT) & ! is.na(SURFACES) &  GAINCONSOCHAUF >0, 
                                     list("Coûts moyens investisstement (euros par m²)" = sum(COUTTOT, na.rm=T)/sum(SURFACES, na.rm = T),
                                          "rdt moyen" = weighted.mean(RDT,w = SURFACES , na.rm = T),
                                          "Gains en % de la consommation initiale de chauffage" =  weighted.mean(GAINCONSOCHAUF/CONSO_CHAUFF,w = SURFACES , na.rm = T), 
                                          "Gains moyens (kwEF par m² par an)" = sum(GAINCONSOCHAUF, na.rm=T)/sum(SURFACES, na.rm = T),
                                          "Coût d'investissemet pour 1 kWh (euros par kwEF par an" = sum(COUTTOT, na.rm = T)/sum(GAINCONSOCHAUF, na.rm=T),
                                          "Gains moyens (en % de la consommation initiale de chauffage + 32 kWh)" =
                                            weighted.mean(GAINCONSOCHAUF/(CONSO_CHAUFF +32*SURFACES), na.rm=T,w=SURFACES)
                                     ), 
                                     by=c("ENERGIE_FIN","SYSTEME_CHAUD_FIN","COD_SYST_ENER")]



tableau_cout_moy_syst  = merge(tableau_cout_moy_syst ,DV_systchaud, by="COD_SYST_ENER")

tableau_cout_moy_syst  =  tableau_cout_moy_syst[SYSTEME_CHAUD_FIN %in% c("Chaudière gaz","Chaudière condensation gaz",
                                               "Chaudière fioul","Chaudière condensation fioul",
                                               "PAC","PAC performant",
                                               "Rooftop", "Rooftop performant",
                                               "Autre système centralisé",  "Autre système centralisé performant")]


tableau_cout_moy_syst[, SYSTEME_CHAUD_FIN := factor(SYSTEME_CHAUD_FIN, levels = c("Chaudière gaz","Chaudière condensation gaz",
                                                           "Chaudière fioul","Chaudière condensation fioul",
                                                           "PAC","PAC performant",
                                                           "Rooftop", "Rooftop performant", 
                                                           "Autre système centralisé",  "Autre système centralisé performant"))]
                                                           
tableau_cout_moy_syst[, SYSTEME_CHAUD_SHORT := factor(SYSTEME_CHAUD_FIN, levels = c("Chaudière gaz","Chaudière condensation gaz",
                                                                                  "Chaudière fioul","Chaudière condensation fioul",
                                                                                  "PAC","PAC performant",
                                                                                  "Rooftop", "Rooftop performant", 
                                                                                  "Autre système centralisé",  
                                                                                  "Autre système centralisé performant"), 
                                                      labels = c("Chaud Gaz","Cond Gaz","Chaud fioul","Cond fioul","PAC","PAC perf", 
                                                                 "Rooftop","Rooftop perf","Au central","Au central perf"))]


                                                           
tableau_cout_moy_syst = tableau_cout_moy_syst[order(SYSTEME_CHAUD_FIN)]
tableau_cout_moy_syst[, COD_SYST_ENER := NULL]

###### graph couts moyens en fonction des gains et comparaison avec sauts de classe ResIRf #######

setnames(tableau_cout_moy_geste, c("geste","cout","gainp","gainu","coutu","gainp2","DV"))
setnames(tableau_cout_moy_syst, c("energie","syst","cout","rdt","gainp","gainu","coutu","gainp2","DV","syst_short"))

cout_renov_resIRF = fread("L://3 - MA3/Batiment/Etudes/Courbes d'abattements ERNR/cout_renov_resIRF.csv")
gain_renov_resIRF = fread("L://3 - MA3/Batiment/Etudes/Courbes d'abattements ERNR/gain_renov_resIRF.csv")

cout_renov_resIRF = melt(cout_renov_resIRF,id.vars = "etiquette_origine",
                         variable.name = "etiquette_finale", 
                         value.name = "cout")
#gain_renov_resIRF = melt(gain_renov_resIRF,id.vars = "etiquette_origine",
#                         variable.name = "etiquette_finale", 
#                         value.name = "gain")
#cout_renov_resIRF = merge(cout_renov_resIRF, gain_renov_resIRF,
#                         by=c("etiquette_origine","etiquette_finale"))

conso_moyenne = data.table( etiquette_origine = c("A","B","C","D","E","F","G"), conso_dpe = c(40,80,120,190,280,380,590))

cout_renov_resIRF = merge(cout_renov_resIRF, conso_moyenne ,
                          by=c("etiquette_origine"))
cout_renov_resIRF = merge(cout_renov_resIRF, conso_moyenne[, list(etiquette_finale = etiquette_origine, conso_dpe_finale = conso_dpe)] ,
                          by=c("etiquette_finale"))
cout_renov_resIRF[,gain := conso_dpe - conso_dpe_finale]
cout_renov_resIRF[,gainp := gain/conso_dpe]
cout_renov_resIRF = cout_renov_resIRF[gain>0]
cout_renov_resIRF[, saut := paste(etiquette_origine,etiquette_finale,sep="-")]
fitdataResIRF = data.table(gain = seq(0,1,0.01))
fitdataResIRF[,cout := 27.31*exp(2.8543*gain)]

graph_comp_RESIRF = ggplot() + geom_line(data = fitdataResIRF , aes(gain,cout)) + mytheme_facet_plot + 
  geom_point(data = tableau_cout_moy_geste, aes(gainp, cout, label = geste),color = "red") + 
  geom_text(data = tableau_cout_moy_geste, aes(gainp, cout, label = geste), vjust = -1,color = "red") +
  geom_point(data = cout_renov_resIRF, aes(gainp,cout),color = "blue") +
  geom_text(data = cout_renov_resIRF, aes(gainp,cout, label = saut),color = "blue", vjust = -1)

graph_comp_RESIRF

graph_comp_RESIRF2 = ggplot() + geom_line(data = fitdataResIRF , aes(gain,cout)) + mytheme_facet_plot + 
  geom_point(data = tableau_cout_moy_geste, aes(gainp, cout, label = geste),color = "red") + 
  geom_text(data = tableau_cout_moy_geste, aes(gainp, cout, label = geste), vjust = -1,color = "red", size = 8) +
  geom_point(data = cout_renov_resIRF, aes(gainp,cout),color = "blue") +
  geom_text(data = cout_renov_resIRF, aes(gainp,cout, label = saut),color = "blue", vjust = -1, size = 8) +
  geom_point(data = tableau_cout_moy_syst, aes(gainp, cout),color = "orange") +
  geom_text(data =  tableau_cout_moy_syst, aes(gainp, cout, label = syst_short), vjust = -1,color = "orange", size = 8)

graph_comp_RESIRF2

##### gains moyens en ajoutant un talon d'ECS et de clim aux conso

graph_comp_RESIRF3 = ggplot() + geom_line(data = fitdataResIRF , aes(gain,cout)) + mytheme_facet_plot + 
  geom_point(data = tableau_cout_moy_geste, aes(gainp2, cout, label = geste),color = "red") + 
  geom_text(data = tableau_cout_moy_geste, aes(gainp2, cout, label = geste), vjust = -1,color = "red", size = 8) +
  geom_point(data = cout_renov_resIRF, aes(gainp,cout),color = "blue") +
  geom_text(data = cout_renov_resIRF, aes(gainp,cout, label = saut),color = "blue", vjust = -1, size = 8) +
  geom_point(data = tableau_cout_moy_syst, aes(gainp2, cout),color = "orange") +
  geom_text(data =  tableau_cout_moy_syst, aes(gainp2, cout, label = syst_short), vjust = -1,color = "orange", size = 8)

graph_comp_RESIRF3


##### coûts en fonction de l'étiquette de départ 

cout_geste_etiq = merge(calc_renta,etiq_ini[,list(COD_BRANCHE,  COD_SS_BRANCHE, COD_BAT_TYPE, COD_OCCUPANT, COD_PERIODE_DETAIL,
                                                  COD_PERIODE_SIMPLE, 
                                                  consou_clim = CONSO_CLIM_EP/SURFACES, 
                                                  consou_ecl = BESOIN_ECLAIRAGE_EP/SURFACES, ETIQUETTE),], 
                                            by=c("COD_BRANCHE", "COD_SS_BRANCHE", "COD_BAT_TYPE", "COD_OCCUPANT", "COD_PERIODE_DETAIL", 
                                                 "COD_PERIODE_SIMPLE"), all.x=T)
                        

names(cout_geste_etiq)
coltokeep = c("ID","COD_BRANCHE","BRANCHE", "ENERGIE","COD_BAT_TYPE", "COD_PERIODE_SIMPLE","COD_EQ_CLIM", "SURFACES","CONSO_CHAUFF",
              "CONSO_CHAUFF_EP", "BESOIN_AUXILIAIRES_CALC","BESOIN_VENTILATION", "consou_clim","consou_ecl",
              paste0("GAIN_",listgeste ), 
              paste0("VAN_",listgeste ), 
              paste0("COUTTOT_", listgeste), 
              paste0("GAINCONSOCHAUF_", listgeste))



cout_geste_etiq = melt(cout_geste_etiq[, coltokeep, with=F], id.vars = c("ID","COD_BRANCHE","BRANCHE", "ENERGIE","COD_BAT_TYPE","COD_PERIODE_SIMPLE","COD_EQ_CLIM", "SURFACES","CONSO_CHAUFF",
                                                                         "CONSO_CHAUFF_EP", "BESOIN_AUXILIAIRES_CALC","BESOIN_VENTILATION", "consou_clim","consou_ecl"))

cout_geste_etiq[,var := sapply(strsplit(as.character(variable), split = "_"),"[", 1),]
cout_geste_etiq[,geste1 := sapply(strsplit(as.character(variable), split = "_"),"[", 2),]
cout_geste_etiq[,geste2 := sapply(strsplit(as.character(variable), split = "_"),"[", 3),]
cout_geste_etiq[!is.na(geste2) ,geste := paste(geste1,geste2,sep="_"), by="ID"]
cout_geste_etiq[is.na(geste2) ,geste := geste1, by="ID"]

cout_geste_etiq[,CONSOU_RT_EP := (consou_clim*SURFACES + consou_ecl*SURFACES + CONSO_CHAUFF_EP + BESOIN_AUXILIAIRES_CALC*2.58 +
                    BESOIN_VENTILATION*2.58)/SURFACES]

cout_geste_etiq = dcast.data.table(cout_geste_etiq, ID +COD_BRANCHE+ BRANCHE +COD_BAT_TYPE+ ENERGIE + COD_PERIODE_SIMPLE + COD_EQ_CLIM + 
                                     SURFACES + CONSO_CHAUFF_EP + CONSOU_RT_EP + geste ~ var )

cout_geste_etiq = merge(cout_geste_etiq, Typebatiment_etiq[,list(COD_BRANCHE = BRANCHE,COD_BAT_TYPE = BAT_TYPE, CATEGORIE_ETIQUETTE)], 
                 by=c("COD_BRANCHE","COD_BAT_TYPE"),all.x=T )

cout_geste_etiq[,ETIQUETTE := check_etiq(CONSOU_RT_EP, CATEGORIE_ETIQUETTE),by="ID"]

ggplot(cout_geste_etiq[COUTTOT >0]) + 
  geom_boxplot(aes(geste,COUTTOT/SURFACES, color = ETIQUETTE))  + theme(text = element_text(size = 20)) +
  mytheme_facet_plot +
  theme(axis.text.x = element_text(angle = 45, size = 20, hjust = 1))

coutmoy_gest_etiq = cout_geste_etiq[!is.na(COUTTOT) & ! is.na(SURFACES), 
                           list("cout" = sum(COUTTOT, na.rm=T)/sum(SURFACES, na.rm = T),
                                "gainp" = weighted.mean(GAIN,w = SURFACES , na.rm = T),
                                "gainu" = sum(GAINCONSOCHAUF, na.rm=T)/sum(SURFACES, na.rm = T),
                                "coutkWhEF" = sum(COUTTOT, na.rm = T)/sum(GAINCONSOCHAUF, na.rm=T)),
                           by=c("geste", "ETIQUETTE")]


ggplot(coutmoy_gest_etiq) + geom_point(aes(gainp, cout, color = ETIQUETTE)) +
  geom_line(data = fitdataResIRF , aes(gain,cout)) 

ggplot(coutmoy_gest_etiq) + geom_point(aes(gainp, cout, color = ETIQUETTE)) +
  geom_line(data = fitdataResIRF , aes(gain,cout)) +
geom_text(data = coutmoy_gest_etiq, aes(gainp, cout, label = geste), vjust = -1,color = "red", size = 8) 



##### coût en fonction de la surface moyenne des bâtiments 
summary(surf_moy_bat)
cout_rdt_chauff = merge(cout_rdt_chauff, surf_moy_bat, by=c("COD_BRANCHE", "COD_SS_BRANCHE","COD_BAT_TYPE"))
cout_rdt_chauff[,COUTINST := COUT*SURFACE]
cout_geste_etiq [, COD_SS_BRANCHE:=substring(ID, 3,4)]
cout_geste_etiq = merge(cout_geste_etiq, surf_moy_bat, by=c("COD_BRANCHE", "COD_SS_BRANCHE","COD_BAT_TYPE"))

ggplot(cout_rdt_chauff[PERIODE == "0" & COUT >0 ,]) + 
  geom_point(aes(SURFACE, COUT, color = ))  + theme(text = element_text(size = 20))  +
  mytheme_facet_plot +
  theme(axis.text.x = element_text(angle = 45, size = 20, hjust = 1))

ggplot(cout_rdt_chauff[PERIODE == "0" & COUT >0 ,]) + 
  geom_point(aes(SURFACE, COUT))  + theme(text = element_text(size = 20)) + facet_wrap(~PRODUCTION_CHAUD) +
  mytheme_facet_plot +
  theme(axis.text.x = element_text(angle = 45, size = 20, hjust = 1))

ggplot(cout_rdt_chauff[PERIODE == "0" & COUT >0  & SURFACE <500,]) + 
  geom_point(aes(PRODUCTION_CHAUD, COUT))  + theme(text = element_text(size = 20)) +
  mytheme_facet_plot +
  theme(axis.text.x = element_text(angle = 45, size = 20, hjust = 1))


ggplot(cout_rdt_chauff[PERIODE == "0" & COUT >0  & SURFACE <500,]) + 
  geom_boxplot(aes(PRODUCTION_CHAUD, COUTINST))  + theme(text = element_text(size = 20)) +
  mytheme_facet_plot +
  theme(axis.text.x = element_text(angle = 45, size = 20, hjust = 1))

ggplot(cout_rdt_chauff[PERIODE == "0" & COUT >0  & SURFACE >500 &  SURFACE <2000,]) + 
  geom_boxplot(aes(PRODUCTION_CHAUD, COUTINST))  + theme(text = element_text(size = 20)) +
  mytheme_facet_plot +
  theme(axis.text.x = element_text(angle = 45, size = 20, hjust = 1))


#####
m = list()
m[[1]] = lm(log(COUT) ~ log(RDT), data = cout_rdt_chauff)
m[[2]] = lm(log(COUT) ~ log(RDT) + log(SURFACE), data = cout_rdt_chauff)
m[[3]] = lm(log(COUT) ~ log(RDT) + log(SURFACE) + BRANCHE.x, data = cout_rdt_chauff)
m[[4]] = lm(log(COUT) ~ log(RDT) + log(SURFACE) + BRANCHE.x + PRODUCTION_CHAUD, data = cout_rdt_chauff)


m2 = list()
m2[[1]] = lm(log(COUTTOT/SURFACES) ~ GAIN , data= cout_geste_etiq[GAIN>0])
m2[[2]] = lm(log(COUTTOT/SURFACES) ~ GAIN + log(SURFACE), data= cout_geste_etiq[GAIN>0])
m2[[3]] = lm(log(COUTTOT/SURFACES) ~ GAIN + log(SURFACE) + BRANCHE.x, data= cout_geste_etiq[GAIN>0])
m2[[4]] = lm(log(COUTTOT/SURFACES) ~ GAIN + log(SURFACE) + BRANCHE.x + log(CONSO_CHAUFF_EP/SURFACES), data= cout_geste_etiq[GAIN>0])
m2[[5]] = lm(log(COUTTOT/SURFACES) ~ GAIN + log(SURFACE) + BRANCHE.x + log(CONSO_CHAUFF_EP/SURFACES) + COD_PERIODE_SIMPLE, data= cout_geste_etiq[GAIN>0])
m2[[6]] = lm(log(COUTTOT/SURFACES) ~ GAIN + log(SURFACE) + BRANCHE.x + log(CONSO_CHAUFF_EP/SURFACES), data= cout_geste_etiq[GAIN>0])
m2[[7]] = lm(log(COUTTOT/SURFACES) ~ GAIN + log(SURFACE) + COD_BAT_TYPE + log(CONSO_CHAUFF_EP/SURFACES), data= cout_geste_etiq[GAIN>0])

screenreg(m2)




