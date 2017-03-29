$.notify.addStyle('releaser', {
  html: "<h1><span data-notify-text/></h1>",
  classes: {
    base: {
      "white-space": "nowrap",
      "color": "white",
      "background-color": "green",
      "padding": "10px 15px",
      "margin-left": "15vw"
    },
    problem: {
      "background-color": "red"
    }
  }
});

function callReleaseInfo( nIdReleaseContext,progressId,artifactId)
{
    $.ajax({
        url: "jsp/admin/plugins/releaser/ReleaseComponentJson.jsp?view=releaseInfoJson&id_context="+nIdReleaseContext,
        type: "GET",
        dataType : "json",
        success: function( data ) {
         if (data.status == 'OK') {
           if(data.result.commandResult)
              {
                 if(artifactId==artifactIdInProgress)
                {
                  $("#console_wf_log").html(data.result.commandResult.log);
                }
               if ( data.result.commandResult.running )
               {
                 /* still running */
               setTimeout(  function(){callReleaseInfo(nIdReleaseContext,progressId,artifactId);}, 5000 );
               }
               else
              {
               if(data.result.commandResult.status==1)
                {
                 $('#release-result-'+artifactId).html('<strong class="text-success"><i class="fa fa-check"></i> Version releasée ! </strong>');
                 $( '#histo-'+artifactId ).notify("Release ok", { position:"right", style:"releaser" } );
               }
               else
               {
                 $('#release-result-'+artifactId).html('<strong class="text-danger"><i class="fa fa-warning"></i> Erreur de release </strong>');
                 $( '#histo-'+artifactId ).notify("Une erreur est intervenue..", { position:"right", style:"releaser", className:"problem" } );
               }
               $(progressId).hide();
            }
             $( progressId +" .progress-bar").attr("style","width:"+data.result.commandResult.progressValue+"%");
            }
             else
            {
           /*reload*/
         setTimeout(  function(){callReleaseInfo(nIdReleaseContext,progressId,artifactId);}, 5000 );
        }
      }
      else if ( data.status == 'ERROR'  )
     {
     }
    }
  });
}
