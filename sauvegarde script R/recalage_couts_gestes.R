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

Cout_parc_Etat_ini <- besoins_chauffage_init

Cout_parc_Etat_ini$GESTE = "Etat initial"
Cout_parc_Etat_ini[,COUT:= 0]
Cout_parc_Etat_ini[,GAIN := 0]

Cout_parc= Cout_parc_Etat_ini
gestetmp = "FENMOD"


for(gestetmp in unique(bibli_couts[EXIGENCE != "Maximale",GESTE])){
  if(gestetmp != "Etat initial"){
    Cout_parc_tmp = besoins_chauffage_init
    Cout_parc_tmp  = merge(Cout_parc_tmp, bibli_couts[EXIGENCE != "Maximale" & GESTE == gestetmp,
                                                      list(ID_AGREG , 
                                                           GESTE,COUT, GAIN)], by="ID_AGREG", all.x=T)
    
    Cout_parc = rbind(Cout_parc,Cout_parc_tmp)
    
  }
}




Cout_parc = merge(Cout_parc, COD_BRANCHE, by="COD_BRANCHE", all.x=T)
Cout_parc = merge(Cout_parc, COD_SS_BRANCHE, by="COD_SS_BRANCHE", all.x=T)
Cout_parc = merge(Cout_parc, COD_BAT_TYPE, by="COD_BAT_TYPE", all.x=T)
Cout_parc = merge(Cout_parc, COD_PERIODE_DETAIL, by="COD_PERIODE_DETAIL", all.x=T)
Cout_parc = merge(Cout_parc, COD_PERIODE_SIMPLE, by="COD_PERIODE_SIMPLE", all.x=T)


#####  listes des bâtiments avec seulement GTB

Cout_parc[COUT == 0 & GESTE != "Etat initial", .N, by="BAT_TYPE"]
Cout_parc[BAT_TYPE == "Piscine" & (COUT !=0 | GESTE == "Etat initial"), .N, by="GESTE"]
Cout_parc[,unique(BAT_TYPE)][Cout_parc[,unique(BAT_TYPE)]  %in% Cout_parc[COUT != 0 & GESTE != "Etat initial" & GESTE != "GTB",unique(BAT_TYPE)] == F]

###
liste_bat = unique(Cout_parc$BAT_TYPE)


Cout_parc[BAT_TYPE == liste_bat[1] & GESTE == "ENSBBC"]

##### couts et gains moyens par bat_type
COUT_ens_moy = Cout_parc[COUT !=0,list(COUT_moy = mean(COUT),
                                   COUT_moy2 = sum(COUT*`SURFACES 2009`)/sum(`SURFACES 2009`),
                        GAIN_moy = mean(GAIN),
                        GAIN_moy2 = sum(BESOIN/`RDT systeme`*GAIN)/sum(BESOIN/`RDT systeme`)), 
                     by=c("GESTE")][order(GESTE)]

COUT_bat_moy = Cout_parc[COUT !=0,list(COUT_moy = mean(COUT),
                        COUT_moy2 = sum(COUT*`SURFACES 2009`)/sum(`SURFACES 2009`), 
                        GAIN_moy = mean(GAIN),
                        GAIN_moy2 = sum(BESOIN/`RDT systeme`*GAIN)/sum(BESOIN/`RDT systeme`)), 
          by=c("BAT_TYPE","GESTE")][order(GESTE)]


ggplot(COUT_bat_moy[BAT_TYPE %in% unique(COD_BAT_TYPE$BAT_TYPE)[1:9]]) + geom_point(aes(GAIN_moy2,COUT_moy2, color=GESTE)) + facet_wrap(~BAT_TYPE)


###### graph couts moyens en fonction des gains et comparaison avec sauts de classe ResIRf #######

cout_renov_resIRF = fread("L://3 - MA3/Batiment/Etudes/Courbes d'abattements ERNR/cout_renov_resIRF.csv")
gain_renov_resIRF = fread("L://3 - MA3/Batiment/Etudes/Courbes d'abattements ERNR/gain_renov_resIRF.csv")

cout_renov_resIRF = melt(cout_renov_resIRF,id.vars = "etiquette_origine",
                         variable.name = "etiquette_finale", 
                         value.name = "cout")

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
  geom_point(data = cout_renov_resIRF, aes(gainp,cout),color = "blue") +
  geom_text(data = cout_renov_resIRF, aes(gainp,cout, label = saut),color = "blue", vjust = -1)

graph_comp_RESIRF + geom_point(data = COUT_bat_moy[BAT_TYPE %in% unique(COD_BAT_TYPE$BAT_TYPE)[1]], 
                               aes(GAIN_moy2,COUT_moy2, color=GESTE))


graph_comp_RESIRF + geom_point(data = COUT_bat_moy[BAT_TYPE %in% unique(COD_BAT_TYPE$BAT_TYPE)[1:9]], 
                              aes(GAIN_moy2,COUT_moy2, color=GESTE))

graph_comp_RESIRF + geom_point(data = COUT_bat_moy[BAT_TYPE %in% unique(COD_BAT_TYPE$BAT_TYPE)[1:30]], 
                               aes(GAIN_moy2,COUT_moy2, color=GESTE))

graph_comp_RESIRF + geom_point(data = COUT_bat_moy[], 
                               aes(GAIN_moy2,COUT_moy2, color=GESTE))

graph_comp_RESIRF + geom_point(data = COUT_bat_moy[BAT_TYPE %in% unique(COD_BAT_TYPE$BAT_TYPE)[1:30]], 
                               aes(GAIN_moy2,COUT_moy2, color=GESTE)) + 
  geom_line(data = COUT_bat_moy[], 
             aes(GAIN_moy2,COUT_moy2, group ="BAT_TYPE") )
                      
graph_comp_RESIRF + geom_point(data = bibli_couts[COUT!=0], 
                               aes(GAIN,COUT, color=GESTE))

listID = unique(bibli_couts$ID_AGREG)

graph_comp_RESIRF + geom_point(data = bibli_couts[COUT!=0 & ID_AGREG == listID[1]], 
                               aes(GAIN,COUT, color=GESTE), size = 2)
graph_comp_RESIRF + geom_point(data = bibli_couts[COUT!=0 & ID_AGREG == listID[2]], 
                               aes(GAIN,COUT, color=GESTE), size = 2)
graph_comp_RESIRF + geom_point(data = bibli_couts[COUT!=0 & ID_AGREG == listID[3]], 
                               aes(GAIN,COUT, color=GESTE), size = 2)
graph_comp_RESIRF + geom_point(data = bibli_couts[COUT!=0 & ID_AGREG == listID[4]], 
                               aes(GAIN,COUT, color=GESTE), size = 2)
graph_comp_RESIRF + geom_point(data = bibli_couts[COUT!=0 & ID_AGREG == listID[5]], 
                               aes(GAIN,COUT, color=GESTE), size = 2)

graph_comp_RESIRF + geom_point(data = bibli_couts[COUT!=0 & BRANCHE == "01"], 
                               aes(GAIN,COUT, color=GESTE), size = 2)
graph_comp_RESIRF + geom_point(data = bibli_couts[COUT!=0 & BRANCHE == "02"], 
                               aes(GAIN,COUT, color=GESTE), size = 2)

graph_comp_RESIRF + geom_point(data =  COUT_ens_moy, 
                              aes( GAIN_moy2,COUT_moy2, color=GESTE), size = 2)




##### modification du coût de ENSBBC en fonction du cout de ENSMOD et de l'écart de gain entre les deux
bibli_couts_modif <- bibli_couts
bibli_couts_modif <- melt(bibli_couts_modif, id.vars = c("ID_AGREG","GESTE","EXIGENCE","BRANCHE"))
bibli_couts_modif <- dcast.data.table(bibli_couts_modif[variable %in% c("GAIN","COUT")], ID_AGREG~variable +GESTE)


bibli_couts_modif[GAIN_ENSBBC<GAIN_ENSMOD,]

###### si les deux gestes existent
bibli_couts_modif[COUT_ENSMOD !=0 & COUT_ENSBBC !=0, COUT_ENSBBC_mod := COUT_ENSMOD*exp(2.85*(GAIN_ENSBBC-GAIN_ENSMOD))]

### sinon on fait la même chose avec FEN_MUR_MOD
bibli_couts_modif[COUT_ENSMOD ==0 & COUT_ENSBBC !=0 &  COUT_FEN_MURMOD !=0 ,
                  COUT_ENSBBC_mod :=  COUT_FEN_MURMOD*exp(2.85*(GAIN_ENSBBC-GAIN_FEN_MURMOD))]

### sinon on fait la même chose avec FENMOD
bibli_couts_modif[COUT_ENSMOD ==0 & COUT_ENSBBC !=0 &  COUT_FEN_MURMOD ==0 & COUT_FENMOD !=0,
                  COUT_ENSBBC_mod :=  COUT_FENMOD*exp(2.85*(GAIN_ENSBBC-GAIN_FENMOD))]


### sinon on en touche pas
bibli_couts_modif[is.na(COUT_ENSBBC_mod), COUT_ENSBBC_mod :=COUT_ENSBBC]

bibli_couts_modif[COUT_ENSBBC!=0, mean(COUT_ENSBBC)]
bibli_couts_modif[COUT_ENSBBC!=0, mean(COUT_ENSBBC_mod)]


bibli_couts_modif[is.na(COUT_ENSBBC_mod)]



bibli_couts = merge(bibli_couts,bibli_couts_modif[, list(ID_AGREG,COUT_ENSBBC_mod)], by="ID_AGREG")
bibli_couts[GESTE == "ENSBBC",COUT:=COUT_ENSBBC_mod]
bibli_couts[,COUT_ENSBBC_mod:=NULL]

write.table( bibli_couts, "../docs_completementaires_EN/Bibli_geste_bati_recalENSBBC.csv", 
             sep=";", dec=".", quote = T, row.names = F)

graph_comp_RESIRF + geom_point(data = bibli_couts[COUT!=0], 
                               aes(GAIN,COUT, color=GESTE))


