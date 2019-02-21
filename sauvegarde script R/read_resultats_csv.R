if (Sys.getenv("JAVA_HOME")!="")
  Sys.setenv(JAVA_HOME="")
options(java.parameters = "-Xmx12288m")
require(XLConnect)
require(openxlsx)

##### lecture sorties du modèle ##### 

lecture_results_modele_tertiaire = function(wbfiles, wbname = vector()){
  
  
  parc = list()
  Conso = list()
  ConsoRT = list()
  
  PM = list()
  etiquette = list()  
  GES = list()
  COUTS= list()
  COUTS_AUTRES = list()
  CCE = list()
  BESOINS = list()
  BESOINS_NON_RT = list()
  BESOINS_RT_SYST = list()
  SURFACE_CLIM = list()
  
  for(i in 1:length(wbfiles)){
    print(wbname[i])
    
    ##### ouverture excel
    
    if(substring(wbfiles[i],first = nchar(wbfiles[i])-3, nchar(wbfiles[i])) == "xlsx"){
      require(XLConnect)
      require(openxlsx)
      detach(name = "package:XLConnect", unload=TRUE)
      require(openxlsx)
      
      #### noms des scenarios auto si le nom n'est pas défini ####
      if(is.na(wbname[i])){
        wbname[i] = substring(wbfiles[i], nchar(wbfiles[i])-19,nchar(wbfiles[i])-5)
      }
     
      ##### lecture parc 
      parc[[i]] <- read.xlsx(wbfiles[i],sheet = "parcAnnee", check.names = T)
      parc[[i]] = data.table(parc[[i]])
      
      setnames(parc[[i]], paste0("X",2009:2050,".0"), as.character(2009:2050))
      parc[[i]][,scenario := wbname[[i]]]
      
      ##### consommations
      
      Conso[[i]] <-  read.xlsx(wbfiles[i], sheet =  "consoAnnee", check.names = T)
      Conso[[i]] = data.table(Conso[[i]] )
      setnames(Conso[[i]],  paste0("X",2009:2050,".0"), as.character(2009:2050))
      Conso[[i]][,scenario := wbname[[i]]]
      
      
      ##### etiquettes 
      
      etiquette[[i]] <-  read.xlsx(wbfiles[i], sheet = "etiquette", check.names = T)
      etiquette[[i]] = data.table(etiquette[[i]])
      etiquette[[i]][,scenario := wbname[[i]]]
      
      ##### GES
      
      GES[[i]] <- read.xlsx(wbfiles[i], sheet =  "GESAnnee", check.names = T)
      GES[[i]] = data.table(GES[[i]] )
      setnames(GES[[i]],  paste0("X",2009:2050,".0"), as.character(2009:2050))
      GES[[i]][,scenario := wbname[[i]]]
      
      #### coûts chauffage et rénovation
      
      COUTS[[i]] <-  read.xlsx(wbfiles[i], sheet =  "cout", check.names = T)
      COUTS[[i]] = data.table(COUTS[[i]] )
      COUTS[[i]][,scenario := wbname[[i]]]
      
      #### cout autres systèmes (clim, ECS, eclairage)
      
      COUTS_AUTRES[[i]] <- read.xlsx(wbfiles[i], sheet =  "coutSystemesAutres", check.names = T)
      COUTS_AUTRES[[i]] = data.table(COUTS_AUTRES[[i]] )
      COUTS_AUTRES[[i]][,scenario := wbname[[i]]]
      
      ##### CCE
      CCE[[i]] <-  read.xlsx(wbfiles[i], sheet =  "contributionClimat", colNames = F, check.names = T) 
      CCE[[i]] = data.table(CCE[[i]])
      setnames(CCE[[i]], c("annee","CCE"))
      CCE[[i]][,scenario := wbname[[i]]]

      
    } 
    else if(substring(wbfiles[i],first = nchar(wbfiles[i])-3, nchar(wbfiles[i])) == ".xls"){
      
      
      require(XLConnect)
      require(openxlsx)
      detach(name = "package:openxlsx", unload=TRUE)
      require(XLConnect)
    wb <- loadWorkbook(filename = wbfiles[i], create = FALSE)
   
    #### noms des scenarios auto si le nom n'est pas défini ####
    if(is.na(wbname[i])){
      wbname[i] = wb@filename
    }

    ##### lecture parc 
    parc[[i]] <- readWorksheet(wb, sheet = "parcAnnee", header = TRUE)
    parc[[i]] = data.table(parc[[i]])
    setnames(parc[[i]], paste0("X",2009:2050), as.character(2009:2050))
    parc[[i]][,scenario := wbname[[i]]]

    ##### consommations
    
    Conso[[i]] <- readWorksheet(wb, sheet =  "consoAnnee", header = TRUE)
    Conso[[i]] = data.table(Conso[[i]] )
    setnames(Conso[[i]], paste0("X",2009:2050), as.character(2009:2050))
    Conso[[i]][,scenario := wbname[[i]]]
    
    
    ##### etiquettes 
    
    etiquette[[i]] <- readWorksheet(wb, sheet = "etiquette", header = TRUE)
    etiquette[[i]] = data.table(etiquette[[i]])
    etiquette[[i]][,scenario := wbname[[i]]]
    
    ##### GES
  
    GES[[i]] <- readWorksheet(wb, sheet =  "GESAnnee", header = TRUE)
    GES[[i]] = data.table(GES[[i]] )
    setnames(GES[[i]], paste0("X",2009:2050), as.character(2009:2050))
    GES[[i]][,scenario := wbname[[i]]]
    
    #### coûts chauffage et rénovation
    
    COUTS[[i]] <- readWorksheet(wb, sheet =   "cout", header = TRUE)
    COUTS[[i]] = data.table(COUTS[[i]] )
    COUTS[[i]][,scenario := wbname[[i]]]
    
    #### cout autres systèmes (clim, ECS, eclairage)
    
    COUTS_AUTRES[[i]] <- readWorksheet(wb, sheet =  "coutSystemesAutres", header = TRUE)
    COUTS_AUTRES[[i]] = data.table(COUTS_AUTRES[[i]] )
    COUTS_AUTRES[[i]][,scenario := wbname[[i]]]
    
    ##### CCE
    CCE[[i]] <- readWorksheet(wb, sheet =  "contributionClimat", header = F) 
    CCE[[i]] = data.table(CCE[[i]])
    setnames(CCE[[i]], c("annee","CCE"))
    CCE[[i]][,scenario := wbname[[i]]]
    rm(wb)  
    } 
    else {
      ### lecture dossier csv
      parc[[i]] <- fread(paste0(wbfiles[i],"/parcAnnee.csv"),   
                         colClasses = list("character"= c("branche","occupation","periodeSimple",
                                                          "energieChauffage")))

      
      setnames(parc[[i]], paste0("annee",2009:2050), as.character(2009:2050))
      parc[[i]][,scenario := wbname[[i]]]
      
      ##### consommations
      
      Conso[[i]] <-  fread(paste0(wbfiles[i],"/ConsoAnnee.csv"),   
                           colClasses = list("character"= c("branche","occupation",
                                                            "periodeSimple", "energieUsage", "usage",
                                                            "usageSimple")), encoding = "UTF-8")
      
      setnames(Conso[[i]], paste0("annee",2009:2050), as.character(2009:2050))
      Conso[[i]][,scenario := wbname[[i]]]
      
      
      ConsoRT[[i]] <-  fread(paste0(wbfiles[i],"/ConsoRTAnnee.csv"),   
                           colClasses = list("character"= c("branche","occupation",
                                                            "periodeSimple", "energieUsage", "usage",
                                                            "usageSimple")), encoding = "UTF-8")
      
      setnames(ConsoRT[[i]], paste0("annee",2009:2050), as.character(2009:2050))
      ConsoRT[[i]][,scenario := wbname[[i]]]
      Conso[[i]] =rbind(Conso[[i]],ConsoRT[[i]])
      
      ##### GES
      
      GES[[i]] <- fread(paste0(wbfiles[i],"/GESAnnee.csv"), 
                               colClasses = list("character"= c("branche","occupation",
                                                                "periodeSimple", "energieUsage", "usage",
                                                                "usageSimple")), encoding = "UTF-8")
  
      setnames(GES[[i]], paste0("annee",2009:2050), as.character(2009:2050))
      GES[[i]][,scenario := wbname[[i]]]
      
      #### coûts chauffage et rénovation
      
      COUTS[[i]] <- fread(paste0(wbfiles[i],"/couts.csv"),
                          colClasses = list("character"= c("branche","occupant","typeRenovationBatiment",
                                                           "typeRenovationSysteme","cible","annee","reglementation"
                                                          )), encoding = "UTF-8")
      
      
      COUTS[[i]][reglementation == "DÃ©cret" , reglementation :="Décret"]
      # COUTS[[i]] <- data.table(read.csv(paste0(wbfiles[i],"/couts.csv"), 
      #                     colClasses = list("character"= c("branche","occupant","typeRenovationBatiment",
      #                                                      "typeRenovationSysteme","cible","annee"
      #                     )),sep=";",fileEncoding ="UTF-8",  encoding = "UTF-8", header = T))
      # 
      COUTS[[i]][,scenario := wbname[[i]]]
      
      #### cout autres systèmes (clim, ECS, eclairage)
      
      COUTS_AUTRES[[i]] <-fread(paste0(wbfiles[i],"/coutsSystemesAutres.csv"), 
                                colClasses = list("character"= c("branche","occupant","typeRenovationBatiment",
                                                                 "typeRenovationSysteme","cible","annee"
                                )), encoding = "UTF-8")

      COUTS_AUTRES[[i]][,scenario := wbname[[i]]]
      
      #### BESOINS RT  par periode de construction
      BESOINS[[i]] = fread(paste0(wbfiles[i],"/export_BESOIN_RT_PERIODE.csv"), 
                                colClasses = list("character"= c("COD_BRANCHE","COD_PERIODE_SIMPLE",
                                                                 "COD_ENERGIE")), encoding = "UTF-8")
      
      BESOINS[[i]][,scenario := wbname[[i]]]
      
      if("export_BESOIN_RT_PERIODE_SYST.csv" %in% list.files(  paste0(wbfiles[i]))){
        BESOINS_RT_SYST[[i]] = fread(paste0(wbfiles[i],"/export_BESOIN_RT_PERIODE_SYST.csv"), 
                             colClasses = list("character"= c("COD_BRANCHE","COD_PERIODE_SIMPLE",
                                                              "COD_ENERGIE","COD_SYSTEME_CHAUD")), encoding = "UTF-8")
        
        BESOINS_RT_SYST[[i]][,scenario := wbname[[i]]]
        setnames(BESOINS_RT_SYST[[i]], c("COD_BRANCHE","annee","COD_PERIODE_SIMPLE","usage","COD_SYSTEME_CHAUD","COD_ENERGIE","CONSO_TOT",
                                         "BESOIN_TOT","scenario"))
      } else {
        BESOINS_RT_SYST[[i]] <- data.table()
      }

     
      
      BESOINS_NON_RT[[i]] = fread(paste0(wbfiles[i],"/export_CONSO_NON_RT_PERIODE.csv"), 
                                   colClasses =
                                     list("character"=c("COD_BRANCHE","COD_PERIODE_SIMPLE","COD_ENERGIE")), encoding = "UTF-8")
      
      
      ### usages non rt conso_tot = besoin_tot en EF
      BESOINS_NON_RT[[i]][,BESOIN_TOT := CONSO_TOT]
      BESOINS_NON_RT[[i]][,scenario := wbname[[i]]]
      
      #### ON empile les besoins  et les consos pour tous les usage
      BESOINS[[i]] = rbind( BESOINS[[i]],  BESOINS_NON_RT[[i]])

      setnames(BESOINS[[i]], c("COD_BRANCHE","annee","COD_PERIODE_SIMPLE","usage","COD_ENERGIE","CONSO_TOT",
                                                       "BESOIN_TOT","scenario"))
      setnames(BESOINS_NON_RT[[i]], c("COD_BRANCHE","annee","COD_PERIODE_SIMPLE","usage","COD_ENERGIE","CONSO_TOT",
                                      "BESOIN_TOT","scenario"))
      
      
      #### surfaces climatisées
      
      SURFACE_CLIM[[i]] = fread(paste0(wbfiles[i],"/export_SURFACES_CLIM.csv"), 
                       colClasses =
                         list("character"=c("COD_BRANCHE","COD_SYSTEME_FROID", "COD_PERIODE_SIMPLE")))
      
      SURFACE_CLIM[[i]][,scenario := wbname[[i]]]
      
      setnames(SURFACE_CLIM[[i]],
               c("COD_BRANCHE","annee","COD_PERIODE_SIMPLE","COD_SYSTEME_FROID","SURFACES_TOT","scenario"))
      
      
      #### parts de marché des systèmes 
      PM[[i]] = fread(paste0(wbfiles[i],"/part_marche.csv"), 
                      colClasses =
                        list("character"=c("annee","branche","periodeSimple",
                                           "energieChauffage","systeme_chauff")))
      
      PM[[i]][,scenario := wbname[[i]]]
      
    }
    
    
  }
  require(XLConnect)
  require(openxlsx)
  detach(name = "package:openxlsx", unload=TRUE)
  require(XLConnect)
  return(list(parc = parc,Conso=Conso,
              etiquette = etiquette, 
              GES = GES,COUTS = COUTS,
              COUTS_AUTRES =COUTS_AUTRES, 
              CCE = CCE,
              BESOINS =  BESOINS, 
              BESOINS_RT_SYST = BESOINS_RT_SYST,
              SURFACE_CLIM =  SURFACE_CLIM, 
              PM = PM))
}

#####  lecture et modifications des résultats

results = lecture_results_modele_tertiaire(wbfiles,wbname =  wbname)

parc = rbindlist(results$parc)
Conso = rbindlist(results$Conso)
etiquette = rbindlist(results$etiquette)
GES = rbindlist(results$GES)
COUTS = rbindlist(results$COUTS)
COUTS_AUTRES = rbindlist(results$COUTS_AUTRES)
CCE =  rbindlist(results$CCE)
BESOINS = rbindlist(results$BESOINS)
BESOINS_RT_SYST = rbindlist(results$BESOINS_RT_SYST)
PM = rbindlist(results$PM)
SURFACE_CLIM = rbindlist(results$SURFACE_CLIM)


if(is.null(etiquette$scenario) == T  ){
etiquette = data.table(annee=NA, branche=NA,ss_branche=NA,bat_type=NA,occupant=NA,periode_detail=NA,
                       
                       periode_simple=NA,
                         systeme_chauff=NA,systeme_froid=NA,surface=NA,conso_u=NA,etiquette=NA, scenario=NA)

etiquette[,systeme_chauff := as.character(systeme_chauff)]

}

#### annee en facteur

BESOINS[, annee:=as.factor(as.character(annee))]
if(!is.null(BESOINS_RT_SYST$annee)){
BESOINS_RT_SYST[, annee:=as.factor(as.character(annee))]
}
#### Ajouts nom_branche et energie
Conso = merge(Conso,COD_BRANCHE[,list(nom_branche = BRANCHE, branche = COD_BRANCHE) ], by = "branche")
Conso = merge(Conso,COD_ENERGIE[,list(nom_energie = ENERGIE, energieUsage = COD_ENERGIE) ], by = "energieUsage")

Conso = melt(Conso,id.vars = c("branche","nom_branche",
                               "occupation","periodeSimple",
                               "energieUsage",
                               "nom_energie","usage",
                               "usageSimple",
                               "facteurEnergiePrimaire", 
                               "scenario"), variable.name = "annee")

GES = merge(GES,COD_BRANCHE[,list(nom_branche = BRANCHE, branche = COD_BRANCHE) ], by = "branche")
GES = merge(GES,COD_ENERGIE[,list(nom_energie = ENERGIE, energieUsage = COD_ENERGIE) ], by = "energieUsage")

GES = melt(GES,id.vars = c("branche","nom_branche","occupation","periodeSimple","energieUsage","nom_energie","usage","usageSimple",
                           "scenario"), variable.name = "annee")

#### ajouts systeme chauffage
COUTS[typeRenovationSysteme != "Etat initial_NP", COD_SYSTEME_CHAUD := substring(typeRenovationSysteme,14,16)]

COUTS = merge(COUTS, COD_SYSTEME_CHAUD, by="COD_SYSTEME_CHAUD", all.x=T)

###### aggregation des besoins totaux par usage
Besoin_branche_usage_energie_periode = BESOINS
Besoin_branche_usage_energie = Besoin_branche_usage_energie_periode[,
                                                                    list(CONSO_TOT = sum(CONSO_TOT), 
                                                                         BESOIN_TOT = sum(BESOIN_TOT)),
                                                                    by=c("COD_BRANCHE","annee","usage",
                                                                         "COD_ENERGIE","scenario")]

Besoin_branche_usage_energie_periode[, annee:=factor(annee)]

#### Ajout variable neuf/ancien

parc[periodeSimple %in% c("01","02","03"), Type_parc :="E"]
parc[!(periodeSimple %in% c("01","02","03")), Type_parc :="N"]

parc[periodeSimple %in% c("01","02","03","04"), Type_parc_MEDPRO :="E"]
parc[!(periodeSimple %in% c("01","02","03","04")), Type_parc_MEDPRO:="N"]

Conso2 = Conso 
Conso2[periodeSimple %in% c("01","02","03"), Type_parc := "E"]
Conso2[!(periodeSimple %in% c("01","02","03")), Type_parc :="N"]

GES2 = GES 
GES2[periodeSimple %in% c("01","02","03"), Type_parc := "E"]
GES2[!(periodeSimple %in% c("01","02","03")), Type_parc :="N"]

if(is.null(etiquette$scenario) == F  ){
etiquette[periode_simple %in% c("01","02","03"), Type_parc := "E"]
etiquette[!(periode_simple %in% c("01","02","03")), Type_parc :="N"]
}

PM[periodeSimple %in% c("01","02","03"), Type_parc := "E"]
PM[!(periodeSimple %in% c("01","02","03")), Type_parc :="N"]

Besoin_branche_usage_energie_periode[COD_PERIODE_SIMPLE  %in% c("01","02","03","04"), Type_parc_MEDPRO := "E"]
Besoin_branche_usage_energie_periode[!(COD_PERIODE_SIMPLE  %in% c("01","02","03","04")), Type_parc_MEDPRO := "N"]

SURFACE_CLIM[COD_PERIODE_SIMPLE  %in% c("01","02","03","04"), Type_parc_MEDPRO := "E"]
SURFACE_CLIM[!(COD_PERIODE_SIMPLE  %in% c("01","02","03","04")), Type_parc_MEDPRO := "N"]

SURFACE_CLIM[COD_PERIODE_SIMPLE  %in% c("01","02","03"), Type_parc := "E"]
SURFACE_CLIM[!(COD_PERIODE_SIMPLE  %in% c("01","02","03")), Type_parc:= "N"]


#### parc empilé 
parc_melted = melt(parc, id.vars = c("branche","occupation","periodeSimple","energieChauffage","scenario","Type_parc", "Type_parc_MEDPRO"),variable.name = "annee",value.name = "Surface")



#### Ajout Branches MEDPRO

Conso[branche == "01", Branche_MEDPRO := "Bureaux" ]
Conso[branche == "02", Branche_MEDPRO := "Autre"]
Conso[branche == "03", Branche_MEDPRO := "Commerce" ]
Conso[branche == "04", Branche_MEDPRO := "Autre" ]
Conso[branche == "05", Branche_MEDPRO := "Autre" ]
Conso[branche == "06", Branche_MEDPRO := "Santé" ]
Conso[branche == "07", Branche_MEDPRO := "Autre" ]
Conso[branche == "08", Branche_MEDPRO := "Autre" ]

Conso[,Branche_MEDPRO := factor(Branche_MEDPRO,levels =c("Bureaux","Commerce","Santé","Autre"))]
if(is.null(etiquette$scenario) == F  ){
etiquette[branche == "01", Branche_MEDPRO := "Bureaux" ]
etiquette[branche == "02", Branche_MEDPRO := "Autre" ]
etiquette[branche == "03", Branche_MEDPRO := "Commerce" ]
etiquette[branche == "04", Branche_MEDPRO := "Autre" ]
etiquette[branche == "05", Branche_MEDPRO := "Autre" ]
etiquette[branche == "06", Branche_MEDPRO := "Santé" ]
etiquette[branche == "07", Branche_MEDPRO := "Autre" ]
etiquette[branche == "08", Branche_MEDPRO := "Autre" ]
etiquette[,Branche_MEDPRO := factor(Branche_MEDPRO,levels =c("Bureaux","Commerce","Santé","Autre"))]
}
parc_melted [branche == "01", Branche_MEDPRO := "Bureaux" ]
parc_melted [branche == "02", Branche_MEDPRO := "Autre" ]
parc_melted [branche == "03", Branche_MEDPRO := "Commerce"]
parc_melted [branche == "04", Branche_MEDPRO := "Autre" ]
parc_melted [branche == "05", Branche_MEDPRO := "Autre" ]
parc_melted [branche == "06", Branche_MEDPRO := "Santé" ]
parc_melted [branche == "07", Branche_MEDPRO := "Autre" ]
parc_melted [branche == "08", Branche_MEDPRO := "Autre" ]
parc_melted[,Branche_MEDPRO := factor(Branche_MEDPRO,levels =c("Bureaux","Commerce","Santé","Autre"))]


Besoin_branche_usage_energie[COD_BRANCHE == "01", Branche_MEDPRO := "Bureaux" ]
Besoin_branche_usage_energie[COD_BRANCHE  == "02", Branche_MEDPRO := "Autre" ]
Besoin_branche_usage_energie[COD_BRANCHE  == "03", Branche_MEDPRO := "Commerce" ]
Besoin_branche_usage_energie[COD_BRANCHE  == "04", Branche_MEDPRO := "Autre" ]
Besoin_branche_usage_energie[COD_BRANCHE  == "05", Branche_MEDPRO := "Autre" ]
Besoin_branche_usage_energie[COD_BRANCHE  == "06", Branche_MEDPRO := "Santé" ]
Besoin_branche_usage_energie[COD_BRANCHE == "07", Branche_MEDPRO := "Autre" ]
Besoin_branche_usage_energie[COD_BRANCHE  == "08", Branche_MEDPRO := "Autre" ]
Besoin_branche_usage_energie[,Branche_MEDPRO := factor(Branche_MEDPRO,levels =c("Bureaux","Commerce","Santé","Autre"))]

Besoin_branche_usage_energie_periode[COD_BRANCHE == "01", Branche_MEDPRO := "Bureaux" ]
Besoin_branche_usage_energie_periode[COD_BRANCHE  == "02", Branche_MEDPRO := "Autre"]
Besoin_branche_usage_energie_periode[COD_BRANCHE  == "03", Branche_MEDPRO :=  "Commerce" ]
Besoin_branche_usage_energie_periode[COD_BRANCHE  == "04", Branche_MEDPRO := "Autre" ]
Besoin_branche_usage_energie_periode[COD_BRANCHE  == "05", Branche_MEDPRO := "Autre" ]
Besoin_branche_usage_energie_periode[COD_BRANCHE  == "06", Branche_MEDPRO := "Santé" ]
Besoin_branche_usage_energie_periode[COD_BRANCHE == "07", Branche_MEDPRO := "Autre" ]
Besoin_branche_usage_energie_periode[COD_BRANCHE  == "08", Branche_MEDPRO := "Autre" ]
Besoin_branche_usage_energie_periode[,Branche_MEDPRO := factor(Branche_MEDPRO,levels =c("Bureaux","Commerce","Santé","Autre"))]

SURFACE_CLIM[COD_BRANCHE == "01", Branche_MEDPRO := "Bureaux" ]
SURFACE_CLIM[COD_BRANCHE  == "02", Branche_MEDPRO :=  "Autre"]
SURFACE_CLIM[COD_BRANCHE  == "03", Branche_MEDPRO := "Commerce" ]
SURFACE_CLIM[COD_BRANCHE  == "04", Branche_MEDPRO := "Autre" ]
SURFACE_CLIM[COD_BRANCHE  == "05", Branche_MEDPRO := "Autre" ]
SURFACE_CLIM[COD_BRANCHE  == "06", Branche_MEDPRO := "Santé" ]
SURFACE_CLIM[COD_BRANCHE == "07", Branche_MEDPRO := "Autre" ]
SURFACE_CLIM[COD_BRANCHE  == "08", Branche_MEDPRO := "Autre" ]
SURFACE_CLIM[,Branche_MEDPRO := factor(Branche_MEDPRO,levels =c("Bureaux","Commerce","Santé","Autre"))]


#### Ajouts usages MEDPRO

Besoin_branche_usage_energie_periode[usage %in% c("Chauffage","ECS","Cuisson","Autre"), Usage_MEDPRO := "Usages thermiques" ]
Besoin_branche_usage_energie_periode[usage %in% c("Climatisation"), Usage_MEDPRO := "Climatisation" ]
Besoin_branche_usage_energie_periode[usage %in% c("Auxiliaires","Ventilation","Process","Froid_alimentaire","Bureautique","Eclairage"), Usage_MEDPRO := "Electricité spécifique" ]

Besoin_branche_usage_energie_periode[usage %in% c("ECS","Cuisson","Autre"), Usage_MEDPRO2 := "Autres usages thermiques" ]
Besoin_branche_usage_energie_periode[usage %in% c("Climatisation"), Usage_MEDPRO2 := "Climatisation" ]
Besoin_branche_usage_energie_periode[usage %in% c("Chauffage"), Usage_MEDPRO2 := "Chauffage" ]
Besoin_branche_usage_energie_periode[usage %in% c("Auxiliaires","Ventilation","Process","Froid_alimentaire","Bureautique","Eclairage"), Usage_MEDPRO2 :=  "Electricité spécifique" ]

Besoin_branche_usage_energie_periode[,Usage_MEDPRO := factor(Usage_MEDPRO,levels = c("Usages thermiques", "Electricité spécifique","Climatisation"))]
Besoin_branche_usage_energie_periode[,Usage_MEDPRO2 := factor(Usage_MEDPRO2,levels = c("Chauffage","Autres usages thermiques", "Electricité spécifique","Climatisation"))]

Besoin_branche_usage_energie[usage %in% c("Chauffage","ECS","Cuisson","Autre"), Usage_MEDPRO := "Usages thermiques" ]
Besoin_branche_usage_energie[usage %in% c("Climatisation"), Usage_MEDPRO := "Climatisation" ]
Besoin_branche_usage_energie[usage %in% c("Auxiliaires","Ventilation","Process","Froid_alimentaire","Bureautique","Eclairage"), Usage_MEDPRO := "Electricité spécifique" ]

Besoin_branche_usage_energie[usage %in% c("ECS","Cuisson","Autre"), Usage_MEDPRO2 := "Autres usages thermiques" ]
Besoin_branche_usage_energie[usage %in% c("Climatisation"), Usage_MEDPRO2 := "Climatisation" ]
Besoin_branche_usage_energie[usage %in% c("Chauffage"), Usage_MEDPRO2 := "Chauffage" ]
Besoin_branche_usage_energie[usage %in% c("Auxiliaires","Ventilation","Process","Froid_alimentaire","Bureautique","Eclairage"), Usage_MEDPRO2 :=  "Electricité spécifique" ]

Besoin_branche_usage_energie[,Usage_MEDPRO := factor(Usage_MEDPRO,levels = c("Usages thermiques", "Electricité spécifique","Climatisation"))]
Besoin_branche_usage_energie[,Usage_MEDPRO2 := factor(Usage_MEDPRO2,levels = c("Chauffage","Autres usages thermiques", "Electricité spécifique","Climatisation"))]

##### ajouts noms énergie
Besoin_branche_usage_energie = merge(Besoin_branche_usage_energie, COD_ENERGIE,by="COD_ENERGIE")
Besoin_branche_usage_energie[,ENERGIE := factor(ENERGIE,c("Electricité","Gaz","Fioul", "Urbain","Autres"))]

Besoin_branche_usage_energie_periode = merge(Besoin_branche_usage_energie_periode, COD_ENERGIE,by="COD_ENERGIE")
Besoin_branche_usage_energie_periode[,ENERGIE := factor(ENERGIE,c("Electricité","Gaz","Fioul", "Urbain","Autres"))]

if(!is.null(BESOINS_RT_SYST$annee)){
BESOINS_RT_SYST <- merge(BESOINS_RT_SYST, COD_ENERGIE,by="COD_ENERGIE")
BESOINS_RT_SYST[,ENERGIE := factor(ENERGIE,c("Electricité","Gaz","Fioul", "Urbain","Autres"))]
BESOINS_RT_SYST = merge(BESOINS_RT_SYST, COD_SYSTEME_CHAUD, by="COD_SYSTEME_CHAUD", all.x=T)

}

BESOINS <- merge(BESOINS, COD_ENERGIE,by="COD_ENERGIE")
BESOINS[,ENERGIE := factor(ENERGIE,c("Electricité","Gaz","Fioul", "Urbain","Autres"))]
####### Investissements par type de rénovation 
INV_SYST_CHAUD = dcast.data.table(COUTS, scenario +annee ~ typeRenovationSysteme, value.var = "investissement", 
                                  fun.aggregate = function(x){sum(x)/10^6})
INV_SYST_CHAUD[,"Etat initial_NP" := NULL]
INV_SYST_CHAUD = melt(INV_SYST_CHAUD,id.vars = c("scenario","annee"))
INV_SYST_CHAUD[, COD_SYSTEME_CHAUD := substring(variable,14,16)]
INV_SYST_CHAUD = merge(INV_SYST_CHAUD, COD_SYSTEME_CHAUD, by="COD_SYSTEME_CHAUD")

INV_GESTE = dcast.data.table(COUTS, scenario + annee ~ typeRenovationBatiment, value.var = "investissement", 
                             fun.aggregate = function(x){sum(x)/10^6})


INV_GESTE[,"Etat initial":=NULL]
INV_GESTE = melt(INV_GESTE,id.vars = c("scenario","annee"))

INV_GESTE_branche = dcast.data.table(COUTS, scenario + annee + branche~ typeRenovationBatiment, value.var = "investissement", 
                             fun.aggregate = function(x){sum(x)/10^6})


INV_GESTE_branche[,"Etat initial":=NULL]
INV_GESTE_branche = melt(INV_GESTE_branche,id.vars = c("scenario","annee","branche"))


INV_AUTRES_renov = dcast.data.table(COUTS_AUTRES, scenario + annee ~ typeRenovationSysteme, value.var = "investissement", 
                                    fun.aggregate = function(x){sum(x)/10^6})

INV_AUTRES_renov =  melt(INV_AUTRES_renov,id.vars = c("scenario","annee"))

##### investissements au m2
COUTS[, INV_m2 := investissement/surface]
summary(COUTS)

INV_SYST_CHAUD_m2 = COUTS[, list( investissement = sum(investissement),
                                  surface = sum(surface)), by=c("scenario","annee","SYSTEME_CHAUD")]
INV_SYST_CHAUD_m2[, INV_m2 := investissement/surface]

INV_SYST_CHAUD_m2[scenario == scenario_ref ,sum(surface)/10^6,by="annee"]


ggplot(INV_SYST_CHAUD_m2[!is.na(SYSTEME_CHAUD)] ) + geom_line(aes(annee, INV_m2, group = scenario, color = scenario)) + 
  geom_point(aes(annee, INV_m2, color = scenario))+ theme(axis.text.x = element_text(angle = 45, size = 12))+
  facet_wrap(~SYSTEME_CHAUD) + 
  mytheme_facet_plot

COUTS_AUTRES[, INV_m2 := investissement/surface]
summary(COUTS_AUTRES[typeRenovationSysteme == "Climatisation"])

INV_AUTRES_renov_m2 = COUTS_AUTRES[, list( investissement = sum(investissement),
                                           surface = sum(surface)), by=c("scenario","annee","typeRenovationSysteme")]
INV_AUTRES_renov_m2[, INV_m2 := investissement/surface]


