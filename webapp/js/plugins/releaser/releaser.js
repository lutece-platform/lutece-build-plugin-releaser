

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
                         $('#release-result-'+artifactId).html('<span class="label label-success">OK</span>');
                    } 
                     else
                    {
                         $('#release-result-'+artifactId).html('<span class="label label-danger">KO</span>');
                    }
                     $(progressId).toggle();
                     
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