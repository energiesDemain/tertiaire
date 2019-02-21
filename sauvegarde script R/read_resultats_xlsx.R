if (Sys.getenv("JAVA_HOME")!="")
  Sys.setenv(JAVA_HOME="")
options(java.parameters = "-Xmx12288m")
require(XLConnect)
require(openxlsx)

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
    
    if(substring(wbfiles[i],first = nchar(wbfiles[i]), nchar(wbfiles[i])) == "x"){
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

      
    } else {
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
  }
  require(XLConnect)
  require(openxlsx)
  detach(name = "package:openxlsx", unload=TRUE)
  require(XLConnect)
  return(list(parc = parc,Conso=Conso,etiquette = etiquette, GES = GES,COUTS = COUTS,COUTS_AUTRES =COUTS_AUTRES, CCE = CCE))
}

