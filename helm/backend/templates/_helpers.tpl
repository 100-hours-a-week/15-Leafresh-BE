{{- define "leafresh-be.name" -}}
leafresh-be
{{- end }}

{{- define "leafresh-be.fullname" -}}
{{ include "leafresh-be.name" . }}-{{ .Release.Name }}
{{- end }}

{{- define "leafresh-be.serviceAccountName" -}}
{{- if .Values.serviceAccount.name }}
{{ .Values.serviceAccount.name }}
{{- else }}
{{ include "leafresh-be.fullname" . }}
{{- end }}
{{- end }}