/*$.notify.addStyle('releaser', {  html: "<h1><span data-notify-text/></h1>",  classes: {    base: {      "white-space": "nowrap",      "color": "white",      "background-color": "green",      "padding": "10px 15px",      "margin-left": "15vw"    },    problem: {      "background-color": "red"    }  }});*/ toastr.options = {"closeButton": true,"positionClass": "toast-top-right", "newestOnTop": true, "preventDuplicates": false,				"showDuration": "300", "hideDuration": "1000", "timeOut": "5000", "extendedTimeOut": "1000", 				"showEasing": "swing", "hideEasing": "linear", "showMethod": "fadeIn", "hideMethod": "fadeOut"				};				function callReleaseInfo( nIdReleaseContext,progressId,artifactId){    $.ajax({        url: "jsp/admin/plugins/releaser/ReleaseComponentSiteJson.jsp?view=releaseInfoJson&id_context="+nIdReleaseContext,        type: "GET",        dataType : "json",        success: function( data ) {         if (data.status == 'OK') {           if(data.result.commandResult)              {                 if(artifactId==artifactIdInProgress)                {					$("#console_wf_log").html(data.result.commandResult.log);					$("#console_wf_log").animate({ scrollTop: $("#console_wf_log")[0].scrollHeight}, "slow");                }               if ( data.result.commandResult.running )               {					/* still running */					setTimeout(  function(){ callReleaseInfo(nIdReleaseContext,progressId,artifactId);}, 5000 );               }               else              {               if(data.result.commandResult.status==1)                {                 $('#info-'+artifactId).html('<div class="label label-success"><i class="fa fa-check"></i> Version releasée ! </div>');                 /* $( '#histo-'+artifactId ).notify("Release ok", { position:"right", style:"releaser" } ); */				txt = " Release du composant <strong>" + $('#name-'+artifactId).text() +"</strong> effectuée !"; 				toastr["success"]( txt, "Information");                 $( '#deploy_site_release-'+artifactId).show();               }               else if (data.result.commandResult.errorType==0)               {                 $('#info-'+artifactId).html('<div class="label label-success"><i class="fa fa-info"></i> Version releasée !(Erreur non bloquante voir les logs) </div>');                 /* $( '#histo-'+artifactId ).notify("Une erreur est intervenue..", { position:"right", style:"releaser", className:"problem" } ); */				 txt = " Une erreur non bloquante de release est survenue pour le composant <strong>" + $('#name-'+artifactId).text() + "</strong>"; 				 toastr["success"]( txt, "Information");               }               else               {            	   $('#info-'+artifactId).html('<div class="label label-danger"><i class="fa fa-warning"></i> Erreur de release </div>');                   /* $( '#histo-'+artifactId ).notify("Une erreur est intervenue..", { position:"right", style:"releaser", className:"problem" } ); */  				   txt = " Une erreur de release est survenue pour le composant <strong>" + $('#name-'+artifactId).text() + "</strong>";   				   toastr["error"]( txt, "Information");                   }               $(progressId).hide();            }             $( progressId +" .progress-bar").attr("style","width:"+data.result.commandResult.progressValue+"%");            }             else            {			/*reload*/			setTimeout(  function(){callReleaseInfo(nIdReleaseContext,progressId,artifactId);}, 5000 );		   }      }      else if ( data.status == 'ERROR'  )     {     }    }  });}