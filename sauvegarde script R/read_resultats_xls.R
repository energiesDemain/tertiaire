if (Sys.getenv("JAVA_HOME")!="")
  Sys.setenv(JAVA_HOME="")
options(java.parameters = "-Xmx12288m")
require(XLConnect)


##### lecture sorties du modèle ##### 

lecture_results_modele_tertiaire = function(wbfiles, wbname = vector()){
  
  
  parc = list()
  Conso = list()
  etiquette= list()
  GES = list()
  COUTS= list()
  COUTS_AUTRES = list()
  CCE = list()
  
  for(i in 1:length(wbfiles)){
    print(wbname[i])
    ##### ouverture excel
  
    wb <- loadWorkbook(wbfiles[i], create = FALSE)
    
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
    
    COUTS[[i]] <- readWorksheet(wb, sheet =  "Cout", header = TRUE)
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

  return(list(parc = parc,Conso=Conso,etiquette = etiquette, GES = GES,COUTS = COUTS,COUTS_AUTRES =COUTS_AUTRES, CCE = CCE))
}

